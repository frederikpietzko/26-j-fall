package com.example.travel.review

import com.example.travel.trip.TripService
import com.example.travel.trip.TripStatus
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

/**
 * The asynchronous review stage: runs after the submit transaction commits,
 * hands the trip to the selected agent framework and records the outcome.
 */
@Component
class ReviewJob(
    reviewers: List<TripReviewer>,
    private val trips: TripService,
    private val reviews: TripReviewRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val reviewers = reviewers.associateBy { it.agent }

    @Async
    @TransactionalEventListener
    fun on(event: TripSubmitted) {
        val reviewer = reviewers.getValue(event.agent)
        trips.updateStatus(event.tripId, TripStatus.IN_REVIEW)
        val started = System.currentTimeMillis()
        log.info("Reviewing trip {} with {}", event.tripId, event.agent)

        val review = runCatching { reviewer.review(event.tripId) }
            .onFailure { log.error("Review of trip {} with {} failed", event.tripId, event.agent, it) }
            .getOrNull()

        reviews.save(
            TripReview(
                tripId = event.tripId,
                agent = event.agent,
                verdict = review?.verdict,
                summary = review?.summary ?: "Review failed - see logs",
                violations = review?.violations.orEmpty(),
                durationMs = System.currentTimeMillis() - started,
            )
        )
        trips.updateStatus(event.tripId, review?.verdict?.toStatus() ?: TripStatus.REVIEW_FAILED)
        log.info("Trip {} reviewed by {}: {}", event.tripId, event.agent, review?.verdict)
    }

    private fun Verdict.toStatus() = when (this) {
        Verdict.APPROVED -> TripStatus.APPROVED
        Verdict.NEEDS_CHANGES -> TripStatus.NEEDS_CHANGES
        Verdict.REJECTED -> TripStatus.REJECTED
    }
}
