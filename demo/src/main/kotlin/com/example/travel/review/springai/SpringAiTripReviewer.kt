package com.example.travel.review.springai

import com.example.travel.review.*
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.ChatClientRequest
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.client.advisor.api.CallAdvisor
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain
import org.springframework.ai.chat.client.entity
import org.springframework.ai.document.Document
import org.springframework.ai.evaluation.EvaluationRequest
import org.springframework.ai.evaluation.Evaluator
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component

/** Tools are plain methods - Spring AI derives the JSON schema from the signature. */
@Component
class SpringAiTravelTools(private val facts: TravelFacts) {

    @Tool(description = "Load a submitted trip with all hotel, flight and train bookings")
    fun getTrip(@ToolParam(description = "The trip id") tripId: Long): TripSnapshot = facts.trip(tripId)

    @Tool(description = "List all company travel policies")
    fun listPolicies(): List<PolicyText> = facts.policies()

    @Tool(description = "Calculate hotel nights and total costs of a trip")
    fun calculateCosts(@ToolParam(description = "The trip id") tripId: Long): CostBreakdown = facts.costs(tripId)
}

/** A tiny custom advisor: sits around every call like a servlet filter. */
class TimingAdvisor : CallAdvisor {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun adviseCall(request: ChatClientRequest, chain: CallAdvisorChain): ChatClientResponse {
        val started = System.currentTimeMillis()
        val response = chain.nextCall(request)
        val usage = response.chatResponse()?.metadata?.usage
        log.info("LLM call took {} ms, tokens: {}", System.currentTimeMillis() - started, usage?.totalTokens)
        return response
    }

    override fun getName() = "TimingAdvisor"
    override fun getOrder() = 0
}

@Component
class SpringAiTripReviewer(
    builder: ChatClient.Builder,
    tools: SpringAiTravelTools,
    private val facts: TravelFacts,
    private val judge: Evaluator,
) : TripReviewer {

    override val agent = ReviewAgent.SPRING_AI

    private val reviewer: ChatClient = builder.clone()
        .defaultSystem(Prompts.REVIEWER)
        .defaultTools(tools)
        .defaultAdvisors(SimpleLoggerAdvisor(), TimingAdvisor())
        .build()

    override fun review(tripId: Long): ReviewOutcome {
        var draft = reviewer.prompt()
            .user(Prompts.review(tripId))
            .call()
            .entity<ReviewOutcome>()

        // Judge pattern: a second, tool-less model checks the first one's work
        repeat(MAX_REVISIONS) {
            val evaluation = judge.evaluate(evaluationRequest(tripId, draft))
            if (evaluation.isPass) return draft
            draft = reviewer.prompt()
                .user(Prompts.revise(tripId, draft, evaluation.feedback))
                .call()
                .entity<ReviewOutcome>()
        }
        return draft
    }

    /** The trip facts become the evaluation's context documents, the draft is the response under test. */
    private fun evaluationRequest(tripId: Long, draft: ReviewOutcome) = EvaluationRequest(
        Prompts.judgeContext(facts.trip(tripId), facts.policies(), facts.costs(tripId)).map(::Document),
        draft.toString(),
    )

    companion object {
        const val MAX_REVISIONS = 2
    }
}
