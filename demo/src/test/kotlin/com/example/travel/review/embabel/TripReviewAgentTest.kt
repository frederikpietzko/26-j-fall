package com.example.travel.review.embabel

import com.example.travel.fakes.InMemoryWorld
import com.example.travel.review.Judgement
import com.example.travel.review.ReviewOutcome
import com.example.travel.review.Verdict
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Embabel actions and conditions are plain methods - the non-LLM ones are testable without a platform. */
class TripReviewAgentTest {

    private val world = InMemoryWorld()
    private val agent = TripReviewAgent(world.facts, EmbabelTravelTools(world.facts))

    private val draft = ReviewOutcome(Verdict.APPROVED, "Looks fine.")

    @Test
    fun `loading the dossier collects the trip and its costs`() {
        val tripId = world.violatingTrip()

        val dossier = agent.loadDossier(TripReviewRequest(tripId))

        assertEquals("Marko Kovac", dossier.trip.traveler)
        assertEquals(3L, dossier.costs.hotelNights)
        assertEquals(2200.0, dossier.costs.total)
    }

    @Test
    fun `an accepting judgement satisfies only the accept condition`() {
        val judgement = Judgement(accepted = true, feedback = "ok")

        assertTrue(agent.draftAccepted(judgement))
        assertFalse(agent.draftRejected(judgement))
    }

    @Test
    fun `a rejecting judgement satisfies only the reject condition`() {
        val judgement = Judgement(accepted = false, feedback = "wrong")

        assertFalse(agent.draftAccepted(judgement))
        assertTrue(agent.draftRejected(judgement))
    }

    @Test
    fun `accepting keeps the draft unrevised`() {
        val review = agent.acceptDraft(draft, Judgement(accepted = true, feedback = "ok"))

        assertEquals(EmbabelReview(draft, revised = false), review)
    }

    @Test
    fun `the policy tool lists all policies`() {
        val titles = EmbabelTravelTools(world.facts).listPolicies().map { it.title }

        assertEquals(listOf("Hotel budget", "Flight class"), titles)
    }
}
