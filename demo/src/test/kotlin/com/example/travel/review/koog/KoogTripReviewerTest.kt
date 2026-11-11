package com.example.travel.review.koog

import com.example.travel.fakes.InMemoryWorld
import com.example.travel.fakes.Replies
import com.example.travel.fakes.ScriptedChatModel
import com.example.travel.review.Prompts
import com.example.travel.review.Verdict
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.ToolResponseMessage
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse

class KoogTripReviewerTest {

    private val world = InMemoryWorld()
    private val llm = ScriptedChatModel()

    // the same Spring AI bridge the application uses, only on top of the scripted model
    private val reviewer = KoogTripReviewer(KoogConfig().koogPromptExecutor(llm), world.facts)

    @Test
    fun `finishes the graph when the judge accepts the draft`() {
        val tripId = world.violatingTrip()
        llm.reviewerSays(Replies.NEEDS_CHANGES).judgeSays(Replies.ACCEPT)

        val outcome = reviewer.review(tripId)

        assertEquals(Verdict.NEEDS_CHANGES, outcome.verdict)
        assertEquals(listOf("Hotel budget: 520 EUR exceeds 180 EUR"), outcome.violations)
        assertEquals(1, llm.judgePrompts.size)
    }

    @Test
    fun `executes the tools the model asks for and sends the results back`() {
        val tripId = world.violatingTrip()
        llm.reviewerAnswers(
            ScriptedChatModel.toolCall("getTrip", """{"tripId": $tripId}"""),
            AssistantMessage(Replies.NEEDS_CHANGES),
        ).judgeSays(Replies.ACCEPT)

        val outcome = reviewer.review(tripId)

        assertEquals(Verdict.NEEDS_CHANGES, outcome.verdict)
        val toolResults = llm.reviewerPrompts[1].instructions
            .filterIsInstance<ToolResponseMessage>()
            .flatMap { it.responses }
        assertEquals("getTrip", toolResults.single().name())
        assertContains(toolResults.single().responseData(), "Marko Kovac")
    }

    @Test
    fun `loops back to the reviewer with the judge's feedback`() {
        val tripId = world.violatingTrip()
        llm.reviewerSays(Replies.APPROVED, Replies.NEEDS_CHANGES).judgeSays(Replies.REJECT, Replies.ACCEPT)

        val outcome = reviewer.review(tripId)

        assertEquals(Verdict.NEEDS_CHANGES, outcome.verdict)
        assertEquals(2, llm.judgePrompts.size)
        assertContains(llm.reviewerPrompts[1].contents, "You missed the hotel budget violation.")
    }

    @Test
    fun `stops looping after the maximum number of revisions`() {
        val tripId = world.violatingTrip()
        llm.reviewerSays(Replies.APPROVED).judgeSays(Replies.REJECT)

        val outcome = reviewer.review(tripId)

        assertEquals(Verdict.APPROVED, outcome.verdict)
        assertEquals(1 + KoogTripReviewer.MAX_REVISIONS, llm.judgePrompts.size)
    }

    @Test
    fun `the judge never sees the reviewer's conversation`() {
        val tripId = world.violatingTrip()
        llm.reviewerSays(Replies.NEEDS_CHANGES).judgeSays(Replies.ACCEPT)

        reviewer.review(tripId)

        val judgePrompt = llm.judgePrompts.single().contents
        assertContains(judgePrompt, "Mandarin Oriental")
        assertFalse(judgePrompt.contains(Prompts.review(tripId)))
        assertFalse(judgePrompt.contains("compliance officer"))
    }

    @Test
    fun `an unparseable model answer fails the review`() {
        val tripId = world.violatingTrip()
        llm.reviewerSays("I think the trip is fine!")

        assertFails { reviewer.review(tripId) }
    }
}
