---
layout: intro
class: section-intro
---

# The demo

## one boring business process, reviewed by three agents

<!--
- One demo, three frameworks
- Who owns the control flow?
  - Spring AI - my own loop in Kotlin
  - Embabel - declared steps, planner finds the path
  - Koog - my graph, Koog walks it
- Domain stays the same, only the reviewer changes
- Travel expense approval - everyone has it, nobody likes it
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-books />context</div>

# A trip is a plain JPA aggregate with its bookings

```kotlin no-compile
@Entity
class Trip(
    var traveler: String,
    var destination: String,
    var startDate: LocalDate,
    var status: TripStatus = TripStatus.DRAFT,
    @OneToMany(cascade = [CascadeType.ALL])
    val hotels: MutableSet<HotelBooking> = linkedSetOf(),
    @OneToMany(cascade = [CascadeType.ALL])
    val flights: MutableSet<FlightBooking> = linkedSetOf(),
    @OneToMany(cascade = [CascadeType.ALL])
    val trains: MutableSet<TrainBooking> = linkedSetOf(),
)
```

<!--
- Boring Spring Boot 4 service
  - Spring Data JPA, REST, H2
- Hotels, flights, trains
  - prices, dates, classes - facts to check
- Trimmed: purpose, origin, end date, submit timestamp
- Context tile - data already lives here
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-books />context</div>

# The rules are sentences written for people, not for code

```kotlin no-compile
Policy("Hotel budget",
    "Hotels may cost at most 180 EUR per night. " +
    "In London, Paris and Zurich the limit is 250 EUR per night.")

Policy("Rail first",
    "If the train journey takes 4 hours or less, the train must be used " +
    "instead of a flight. Second class only.")
```

<!--
- Five policies: hotel budget, flight class, rail first, lead time, weekend stays
- Why a model
  - prose by HR, full of exceptions
  - if-else for all = boring work
- Policies = table rows
  - POST /api/policies at runtime, e.g. car rental
  - next review picks it up, no redeploy
- Confluence rules checked by hand
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-plugs-connected />integration</div>

# Every agent reads the same facts through one read-only service

```kotlin no-compile
@Service
@Transactional(readOnly = true)
class TravelFacts(private val trips: TripRepository, private val policies: PolicyRepository) {
    fun trip(tripId: Long): TripSnapshot
    fun policies(): List<PolicyText>
    fun costs(tripId: Long): CostBreakdown
}
```

<!--
- Bridge domain - agents
  - each framework exposes these as tools
  - logic here, not in framework code
- readOnly = true
  - agent looks, never changes
  - job writes the result, not the model
- Snapshots, not entities
  - no lazy loading, no Hibernate proxies in prompts
- Costs computed in code
  - models bad at arithmetic
- Bodies omitted - plain mapping
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-brackets-curly />type safety</div>

# Three agents, one contract

```kotlin no-compile
@Serializable
data class ReviewOutcome(
    val verdict: Verdict,
    val summary: String,
    val violations: List<String> = emptyList(),
)

interface TripReviewer {
    val agent: ReviewAgent
    fun review(tripId: Long): ReviewOutcome
}
```

<!--
- Same ReviewOutcome for every framework
  - one implementation each
  - verdict, summary, violations - a type, not a chat message
- Verdict enum: APPROVED, NEEDS_CHANGES, REJECTED
  - maps to trip status
- Red line: how does each framework get to this type?
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-lifebuoy />reliability</div>

# Choosing the framework is one request parameter

```http
POST /api/trips/2/submit?agent=EMBABEL
```

```kotlin no-compile
@Async
@TransactionalEventListener
fun on(event: TripSubmitted) {
    val review = reviewers.getValue(event.agent).review(event.tripId)
    reviews.save(TripReview(event.tripId, event.agent, review))
}
```

<!--
- Submit publishes TripSubmitted, returns 202
- Review after commit, on another thread
  - Async + TransactionalEventListener - plain Spring
  - no DB transaction held while waiting on the model
- getValue(event.agent)
  - all TripReviewer beans injected, picked by parameter
  - SPRING_AI, EMBABEL, KOOG
- Simplified: IN_REVIEW, duration, REVIEW_FAILED
- Seeded trips
  - trip 1: Zagreb - Ljubljana by train, compliant
  - trip 2: Vienna - Munich, business class 1h flight, 520 EUR hotel, 5 days ahead
  - trip 2 in the demo
-->

---
class: graphic-slide
---

# Every reviewer drafts, a judge audits, a rejection goes back

<div class="flow flow-5">
  <div class="flow-node"><ph-robot /><span>reviewer with tools</span></div>
  <div class="flow-arrow"><span>draft</span></div>
  <div class="flow-node"><ph-gavel /><span>judge without tools</span></div>
  <div class="flow-arrow"><span>accepted</span></div>
  <div class="flow-node"><ph-seal-check /><span>ReviewOutcome</span></div>
  <div v-click class="flow-loop reveal"><span>feedback</span></div>
</div>

<!--
- Judge pattern - all three use it
  - reviewer with tools writes a draft
  - independent call audits it against the facts
- Why
  - LLMs confident when wrong
  - fresh context catches many mistakes
  - cheap insurance - reliability tile
- [click] Rejection - feedback back to the reviewer
  - max two revisions
- Same pattern, three different expressions
-->
