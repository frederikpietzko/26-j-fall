package com.example.travel.review

import kotlinx.serialization.Serializable

enum class ReviewAgent { SPRING_AI, EMBABEL, KOOG }

/** Published when a trip is submitted - picked up by [ReviewJob] after commit. */
data class TripSubmitted(val tripId: Long, val agent: ReviewAgent)

@Serializable
enum class Verdict { APPROVED, NEEDS_CHANGES, REJECTED }

/** What every reviewer - regardless of framework - has to produce. */
@Serializable
data class ReviewOutcome(
    val verdict: Verdict,
    val summary: String,
    val violations: List<String> = emptyList(),
)

/** The judge's opinion about a draft review. */
@Serializable
data class Judgement(
    val accepted: Boolean,
    val feedback: String,
)

/** One agent framework, one implementation. */
interface TripReviewer {
    val agent: ReviewAgent
    fun review(tripId: Long): ReviewOutcome
}
