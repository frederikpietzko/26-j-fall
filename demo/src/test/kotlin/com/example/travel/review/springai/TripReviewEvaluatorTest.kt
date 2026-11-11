package com.example.travel.review.springai

import com.example.travel.fakes.Replies
import com.example.travel.fakes.ScriptedChatModel
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.document.Document
import org.springframework.ai.evaluation.EvaluationRequest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TripReviewEvaluatorTest {

    private val llm = ScriptedChatModel()
    private val evaluator = TripReviewEvaluator(ChatClient.builder(llm))
    private val request = EvaluationRequest(
        listOf(Document("Trip: Mandarin Oriental, 520 EUR"), Document("Policies: - Hotel budget: max 180 EUR")),
        "ReviewOutcome(verdict=APPROVED)",
    )

    @Test
    fun `an accepted draft passes`() {
        llm.judgeSays(Replies.ACCEPT)

        val response = evaluator.evaluate(request)

        assertTrue(response.isPass)
        assertEquals("Correct.", response.feedback)
    }

    @Test
    fun `a rejected draft fails with the judge's feedback`() {
        llm.judgeSays(Replies.REJECT)

        val response = evaluator.evaluate(request)

        assertFalse(response.isPass)
        assertEquals("You missed the hotel budget violation.", response.feedback)
    }

    @Test
    fun `the judge sees every context document and the response under test`() {
        llm.judgeSays(Replies.ACCEPT)

        evaluator.evaluate(request)

        val prompt = llm.judgePrompts.single().contents
        assertContains(prompt, "Trip: Mandarin Oriental, 520 EUR")
        assertContains(prompt, "Policies: - Hotel budget: max 180 EUR")
        assertContains(prompt, "Draft review: ReviewOutcome(verdict=APPROVED)")
    }

    @Test
    fun `an unparseable judgement fails the evaluation`() {
        llm.judgeSays("Looks fine to me")

        assertFails { evaluator.evaluate(request) }
    }
}
