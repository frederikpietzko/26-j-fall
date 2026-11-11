package com.example.travel.review.springai

import com.example.travel.review.Judgement
import com.example.travel.review.Prompts
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.client.entity
import org.springframework.ai.evaluation.EvaluationRequest
import org.springframework.ai.evaluation.EvaluationResponse
import org.springframework.ai.evaluation.Evaluator
import org.springframework.stereotype.Component

/**
 * The judge as a Spring AI [Evaluator] - the same contract as the built-in RelevancyEvaluator and
 * FactCheckingEvaluator: context documents and a response in, pass/fail plus feedback out.
 */
@Component
class TripReviewEvaluator(builder: ChatClient.Builder) : Evaluator {

    private val judge: ChatClient = builder.clone()
        .defaultSystem(Prompts.JUDGE)
        .defaultAdvisors(SimpleLoggerAdvisor())
        .build()

    override fun evaluate(request: EvaluationRequest): EvaluationResponse {
        val judgement = judge.prompt()
            .user(Prompts.judge(request.dataList.mapNotNull { it.text }, request.responseContent))
            .call()
            .entity<Judgement>()
        return EvaluationResponse(judgement.accepted, judgement.feedback, emptyMap())
    }
}
