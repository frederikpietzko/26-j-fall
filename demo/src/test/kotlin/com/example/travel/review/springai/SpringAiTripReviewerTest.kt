package com.example.travel.review.springai

import com.example.travel.fakes.InMemoryWorld
import com.example.travel.fakes.Replies
import com.example.travel.fakes.ScriptedChatModel
import com.example.travel.review.ReviewOutcome
import com.example.travel.review.Verdict
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.support.ToolCallbacks
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails

class SpringAiTripReviewerTest {

    private val world = InMemoryWorld()
    private val llm = ScriptedChatModel()
    private val tools = SpringAiTravelTools(world.facts)
    private val reviewer =
        SpringAiTripReviewer(ChatClient.builder(llm), tools, world.facts, TripReviewEvaluator(ChatClient.builder(llm)))

    @Test
    fun `returns the draft when the judge accepts it`() {
        val tripId = world.violatingTrip()
        llm.reviewerSays(Replies.NEEDS_CHANGES).judgeSays(Replies.ACCEPT)

        val outcome = reviewer.review(tripId)

        assertEquals(Verdict.NEEDS_CHANGES, outcome.verdict)
        assertEquals(listOf("Hotel budget: 520 EUR exceeds 180 EUR"), outcome.violations)
        assertEquals(1, llm.reviewerPrompts.size)
    }

    @Test
    fun `revises the draft with the judge's feedback`() {
        val tripId = world.violatingTrip()
        llm.reviewerSays(Replies.APPROVED, Replies.NEEDS_CHANGES).judgeSays(Replies.REJECT, Replies.ACCEPT)

        val outcome = reviewer.review(tripId)

        assertEquals(Verdict.NEEDS_CHANGES, outcome.verdict)
        assertContains(llm.reviewerPrompts.last().contents, "You missed the hotel budget violation.")
    }

    @Test
    fun `gives up revising after the maximum number of rounds`() {
        val tripId = world.violatingTrip()
        llm.reviewerSays(Replies.APPROVED).judgeSays(Replies.REJECT)

        val outcome = reviewer.review(tripId)

        assertEquals(Verdict.APPROVED, outcome.verdict)
        assertEquals(1 + SpringAiTripReviewer.MAX_REVISIONS, llm.reviewerPrompts.size)
        assertEquals(SpringAiTripReviewer.MAX_REVISIONS, llm.judgePrompts.size)
    }

    @Test
    fun `the judge gets the trip facts and the draft, not the tools`() {
        val tripId = world.violatingTrip()
        llm.reviewerSays(Replies.NEEDS_CHANGES).judgeSays(Replies.ACCEPT)

        reviewer.review(tripId)

        val judgePrompt = llm.judgePrompts.single().contents
        assertContains(judgePrompt, "Mandarin Oriental")
        assertContains(judgePrompt, "Hotel budget: Hotels may cost at most 180 EUR per night.")
        assertContains(judgePrompt, ReviewOutcome(Verdict.NEEDS_CHANGES, "Hotel too expensive.", listOf("Hotel budget: 520 EUR exceeds 180 EUR")).toString())
    }

    @Test
    fun `an unparseable model answer fails the review`() {
        val tripId = world.violatingTrip()
        llm.reviewerSays("I think the trip is fine!")

        assertFails { reviewer.review(tripId) }
    }

    @Test
    fun `exposes the travel facts as tools derived from the annotated methods`() {
        val callbacks = ToolCallbacks.from(tools).associateBy { it.toolDefinition.name() }

        assertEquals(setOf("getTrip", "listPolicies", "calculateCosts"), callbacks.keys)
    }

    @Test
    fun `a tool call returns the trip as JSON`() {
        val tripId = world.violatingTrip()
        val getTrip = ToolCallbacks.from(tools).single { it.toolDefinition.name() == "getTrip" }

        val json = getTrip.call("""{"tripId": $tripId}""")

        assertContains(json, "Marko Kovac")
        assertContains(json, "\"cabinClass\":\"BUSINESS\"")
    }
}
