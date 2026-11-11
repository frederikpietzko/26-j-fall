package com.example.travel.review.embabel

import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.annotation.Condition
import com.embabel.agent.api.annotation.LlmTool
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.createObject
import com.embabel.agent.api.invocation.AgentInvocation
import com.embabel.agent.core.AgentPlatform
import com.embabel.agent.spi.support.springai.SpringAiLlmService
import com.example.travel.review.*
import org.springframework.ai.chat.model.ChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

/** Embabel talks to the LLM through the very same Spring AI ChatModel. */
@Configuration
class EmbabelConfig {
    @Bean
    fun gpt5Mini(chatModel: ChatModel) = SpringAiLlmService(
        name = "gpt-5-mini",
        provider = "OpenAI",
        chatModel = chatModel,
    )
}

// --- the domain types that flow over Embabel's blackboard ---

data class TripReviewRequest(val tripId: Long)

data class TripDossier(val trip: TripSnapshot, val costs: CostBreakdown)

data class EmbabelReview(val outcome: ReviewOutcome, val revised: Boolean)

@Component
class EmbabelTravelTools(private val facts: TravelFacts) {

    @LlmTool(description = "List all company travel policies")
    fun listPolicies(): List<PolicyText> = facts.policies()
}

@Agent(description = "Reviews business trips against the company travel policies")
class TripReviewAgent(
    private val facts: TravelFacts,
    private val tools: EmbabelTravelTools,
) {

    /** Plain code is an action too - no LLM needed to load data. */
    @Action
    fun loadDossier(request: TripReviewRequest): TripDossier =
        TripDossier(facts.trip(request.tripId), facts.costs(request.tripId))

    @Action
    fun draftReview(dossier: TripDossier, context: OperationContext): ReviewOutcome =
        context.ai()
            .withDefaultLlm()
            .withSystemPrompt(Prompts.REVIEWER)
            .withToolObject(tools)
            .createObject("Review this trip: $dossier")

    /** The post conditions tell the planner: after judging, we know which branch to take. */
    @Action(post = [DRAFT_ACCEPTED, DRAFT_REJECTED])
    fun judgeDraft(dossier: TripDossier, draft: ReviewOutcome, context: OperationContext): Judgement =
        context.ai()
            .withDefaultLlm()
            .withSystemPrompt(Prompts.JUDGE)
            .createObject(Prompts.judge(dossier.trip, facts.policies(), dossier.costs, draft))

    @Condition(name = DRAFT_ACCEPTED)
    fun draftAccepted(judgement: Judgement): Boolean = judgement.accepted

    @Condition(name = DRAFT_REJECTED)
    fun draftRejected(judgement: Judgement): Boolean = !judgement.accepted

    @AchievesGoal(description = "The trip review was confirmed by the judge")
    @Action(pre = [DRAFT_ACCEPTED])
    fun acceptDraft(draft: ReviewOutcome, judgement: Judgement): EmbabelReview =
        EmbabelReview(draft, revised = false)

    @AchievesGoal(description = "The trip review was corrected after the judge's feedback")
    @Action(pre = [DRAFT_REJECTED])
    fun reviseDraft(
        dossier: TripDossier,
        draft: ReviewOutcome,
        judgement: Judgement,
        context: OperationContext,
    ): EmbabelReview = EmbabelReview(
        context.ai()
            .withDefaultLlm()
            .withSystemPrompt(Prompts.REVIEWER)
            .withToolObject(tools)
            .createObject(Prompts.revise(dossier.trip.id, draft, judgement) + "\nTrip: $dossier"),
        revised = true,
    )

    companion object {
        const val DRAFT_ACCEPTED = "draftAccepted"
        const val DRAFT_REJECTED = "draftRejected"
    }
}

/** Invokes the agent: the platform plans a path from the input type to the goal type. */
@Component
class EmbabelTripReviewer(private val agentPlatform: AgentPlatform) : TripReviewer {

    override val agent = ReviewAgent.EMBABEL

    override fun review(tripId: Long): ReviewOutcome =
        AgentInvocation.create<EmbabelReview>(agentPlatform)
            .invoke(TripReviewRequest(tripId))
            .outcome
}
