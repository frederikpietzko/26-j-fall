package com.example.travel.review.koog

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.builder.subgraph
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import ai.koog.agents.ext.agent.CriticResult
import ai.koog.agents.ext.agent.subgraphWithTask
import ai.koog.agents.ext.agent.subgraphWithVerification
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import com.example.travel.review.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@LLMDescription("Look up trips, policies and costs")
class KoogTravelTools(private val facts: TravelFacts) : ToolSet {

    @Tool
    @LLMDescription("Load a submitted trip with all hotel, flight and train bookings")
    fun getTrip(@LLMDescription("The trip id") tripId: Long): TripSnapshot = facts.trip(tripId)

    @Tool
    @LLMDescription("List all company travel policies")
    fun listPolicies(): List<PolicyText> = facts.policies()

    @Tool
    @LLMDescription("Calculate hotel nights and total costs of a trip")
    fun calculateCosts(@LLMDescription("The trip id") tripId: Long): CostBreakdown = facts.costs(tripId)
}

/**
 * Koog: the review is an explicit graph - Koog's subgraphs for the tool loop and the judge,
 * plain nodes and edges for the revision loop.
 * The PromptExecutor comes from koog-spring-ai - it delegates to Spring AI's ChatModel.
 */
@Component
class KoogTripReviewer(
    private val executor: PromptExecutor,
    private val facts: TravelFacts,
) : TripReviewer {

    override val agent = ReviewAgent.KOOG

    // No Schema capability: the Spring AI bridge does not forward response schemas.
    // Not needed - the subgraphs return their typed results as calls of a finish tool.
    private val model = LLModel(
        provider = LLMProvider.OpenAI,
        id = "gpt-5-mini",
        capabilities = listOf(LLMCapability.Completion, LLMCapability.Tools),
        contextLength = 400_000,
    )

    private val tools = ToolRegistry { tools(KoogTravelTools(facts)) }

    fun reviewStrategy(tripId: Long) = strategy<String, ReviewOutcome>("trip-review") {
        var revisions = 0

        // the tool loop and the structured answer: the model calls tools until it calls the finish tool
        val review by subgraphWithTask<String, ReviewOutcome>(name = "review") { task -> task }

        // fresh history: the judge never sees the reviewer's conversation, which is restored afterwards
        val judge by subgraph<ReviewOutcome, CriticResult<ReviewOutcome>>("judge", freshHistory = true) {
            val verify by subgraphWithVerification<ReviewOutcome>(tools = emptyList()) { draft ->
                llm.writeSession {
                    clearHistory()
                    appendPrompt { system(Prompts.JUDGE) }
                }
                Prompts.judge(facts.trip(tripId), facts.policies(), facts.costs(tripId), draft)
            }
            nodeStart then verify then nodeFinish
        }

        val revise by node<CriticResult<ReviewOutcome>, String>("revise") { critique ->
            revisions++
            Prompts.revise(tripId, critique.input, critique.feedback)
        }

        nodeStart then review then judge

        // judge pattern: accept, or loop back with the judge's feedback
        edge(judge forwardTo nodeFinish onCondition { it.successful || revisions >= MAX_REVISIONS } transformed { it.input })
        edge(judge forwardTo revise onCondition { !it.successful })
        revise then review
    }

    override fun review(tripId: Long): ReviewOutcome = runBlocking {
        AIAgent(
            promptExecutor = executor,
            strategy = reviewStrategy(tripId),
            agentConfig = AIAgentConfig(
                prompt = prompt("trip-review") { system(Prompts.REVIEWER) },
                model = model,
                maxAgentIterations = 200,
            ),
            toolRegistry = tools,
        ).run(Prompts.review(tripId))
    }

    companion object {
        const val MAX_REVISIONS = 2
    }
}
