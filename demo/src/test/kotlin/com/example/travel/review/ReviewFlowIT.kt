package com.example.travel.review

import com.example.travel.IntegrationTest
import com.example.travel.fakes.Replies
import com.example.travel.fakes.ScriptedChatModel
import com.example.travel.fakes.aHotel
import com.example.travel.fakes.aTripRequest
import com.example.travel.trip.TripService
import com.example.travel.trip.TripStatus
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Submit over HTTP, let the background job run the real agent wiring, read the outcome over HTTP. */
@IntegrationTest
class ReviewFlowIT(
    @Autowired private val mvc: MockMvc,
    @Autowired private val trips: TripService,
    @Autowired private val llm: ScriptedChatModel,
) {

    @BeforeEach
    fun resetModel() = llm.reset()

    @ParameterizedTest
    @EnumSource(ReviewAgent::class)
    fun `each agent reviews a submitted trip in the background`(agent: ReviewAgent) {
        val tripId = violatingTrip()
        llm.reviewerSays(Replies.NEEDS_CHANGES).judgeSays(Replies.ACCEPT)

        mvc.post("/api/trips/$tripId/submit?agent=$agent").andExpect {
            status { isAccepted() }
            jsonPath("$.status") { value("SUBMITTED") }
        }

        awaitStatus(tripId, TripStatus.NEEDS_CHANGES)
        mvc.get("/api/trips/$tripId/reviews").andExpect {
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].agent") { value(agent.name) }
            jsonPath("$[0].verdict") { value("NEEDS_CHANGES") }
            jsonPath("$[0].violations[0]") { value("Hotel budget: 520 EUR exceeds 180 EUR") }
        }
        assertTrue(llm.judgePrompts.isNotEmpty())
    }

    @ParameterizedTest
    @EnumSource(ReviewAgent::class)
    fun `each agent revises its draft when the judge rejects it`(agent: ReviewAgent) {
        val tripId = violatingTrip()
        llm.reviewerSays(Replies.APPROVED, Replies.NEEDS_CHANGES).judgeSays(Replies.REJECT, Replies.ACCEPT)

        mvc.post("/api/trips/$tripId/submit?agent=$agent")

        awaitStatus(tripId, TripStatus.NEEDS_CHANGES)
        assertTrue(llm.reviewerPrompts.any { it.contents.contains("You missed the hotel budget violation.") })
    }

    @Test
    fun `an approved trip is locked`() {
        val tripId = violatingTrip()
        llm.reviewerSays(Replies.APPROVED).judgeSays(Replies.ACCEPT)
        mvc.post("/api/trips/$tripId/submit?agent=SPRING_AI")
        awaitStatus(tripId, TripStatus.APPROVED)

        mvc.put("/api/trips/$tripId") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """{"traveler":"X","purpose":"Y","origin":"A","destination":"B","startDate":"2026-12-01","endDate":"2026-12-02"}"""
        }.andExpect { status { isConflict() } }
    }

    @Test
    fun `a failing agent marks the review as failed and the trip can be resubmitted`() {
        val tripId = violatingTrip()
        llm.reviewerSays("Sorry, I cannot produce JSON today.")
        mvc.post("/api/trips/$tripId/submit?agent=SPRING_AI")
        awaitStatus(tripId, TripStatus.REVIEW_FAILED)

        mvc.get("/api/trips/$tripId/reviews").andExpect {
            jsonPath("$[0].verdict") { value(null) }
            jsonPath("$[0].summary") { value("Review failed - see logs") }
        }
        mvc.post("/api/trips/$tripId/submit?agent=SPRING_AI").andExpect { status { isAccepted() } }
    }

    private fun violatingTrip(): Long = trips.create(aTripRequest()).id!!.also { trips.addHotel(it, aHotel()) }

    private fun awaitStatus(tripId: Long, expected: TripStatus) =
        await().atMost(Duration.ofSeconds(20)).untilAsserted { assertEquals(expected, trips.get(tripId).status) }
}
