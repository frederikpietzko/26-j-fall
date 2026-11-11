---
layout: intro
class: section-intro
---

# Embabel

## declare the steps, let the planner find the path

<!--
- Rod Johnson, creator of Spring
- Kotlin, works from Java, on top of Spring AI
- Spring AI : Embabel = Servlet API : Spring MVC
- Control flow: nobody writes it down
  - typed actions + goals, planner derives the order
- Same TravelFacts, ReviewOutcome, judge
- Changes how the 2nd and 3rd agent get built
-->

---
class: graphic-slide
---

# Embabel plans like a game AI: actions, conditions, goals

<div class="flow flow-5">
  <div class="flow-node"><ph-gear /><span>@Action methods</span></div>
  <div class="flow-arrow"><span>types</span></div>
  <div class="flow-node"><ph-path /><span>GOAP planner</span></div>
  <div class="flow-arrow"><span>plan</span></div>
  <div class="flow-node"><ph-flag-checkered /><span>@AchievesGoal</span></div>
  <div v-click class="flow-loop flow-loop-end reveal"><span>replan</span></div>
</div>

<!--
- GOAP - goal oriented action planning
  - from games - F.E.A.R. enemies
  - preconditions, effects, search to the goal
- Preconditions / effects mostly types
  - TripDossier action needs a TripDossier first
  - named conditions where types are not enough
- Planner = algorithm, not LLM
  - deterministic, explainable, logged
- [click] Replan after every action
  - unexpected effect - another route
- Cost and value 0 - 1 per action
  - cheaper route preferred
- Newer: Utility, Supervisor planners, @State machines
-->

---
class: graphic-slide
clicks: 5
---

# A* walks the cheapest chain of actions to the goal and replans when the world changes

<svg class="goap-graph" viewBox="0 90 1400 500" role="img" aria-label="GOAP planning graph searched by A*">
  <defs>
    <linearGradient id="goap-gradient" gradientUnits="userSpaceOnUse" x1="0" y1="0" x2="1400" y2="0">
      <stop offset="0" stop-color="var(--fundamentals-purple)" />
      <stop offset="1" stop-color="var(--fundamentals-pink)" />
    </linearGradient>
    <marker id="goap-head" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
      <path class="goap-head" d="M0,0 L10,5 L0,10 z" />
    </marker>
    <marker id="goap-head-on" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
      <path class="goap-head goap-head-on" d="M0,0 L10,5 L0,10 z" />
    </marker>
  </defs>
  <!-- edges: every action is an edge from its pre state to its post state -->
  <g class="goap-edge" :class="{ 'is-on': $clicks >= 1 }">
    <path class="goap-base" d="M200,364 L360,206" :marker-end="$clicks >= 1 ? 'url(#goap-head-on)' : 'url(#goap-head)'" />
    <path class="goap-hl" pathLength="1" d="M200,364 L360,206" />
    <text x="262" y="270" text-anchor="end">loadDossier · 1</text>
  </g>
  <g class="goap-edge goap-edge-alt" :class="{ 'is-seen': $clicks >= 1, 'is-off': $clicks >= 2 }">
    <path class="goap-base" d="M240,400 L620,400" marker-end="url(#goap-head)" />
    <path class="goap-hl" pathLength="1" d="M240,400 L620,400" />
    <text x="432" y="440" text-anchor="middle">draftWithTools · 8</text>
  </g>
  <g class="goap-edge" :class="{ 'is-on': $clicks >= 2 }">
    <path class="goap-base" d="M510,206 L660,360" :marker-end="$clicks >= 2 ? 'url(#goap-head-on)' : 'url(#goap-head)'" />
    <path class="goap-hl" pathLength="1" d="M510,206 L660,360" />
    <text x="565" y="335" text-anchor="end">draftReview · 3</text>
  </g>
  <g class="goap-edge" :class="{ 'is-on': $clicks >= 3 }">
    <path class="goap-base" d="M820,364 L960,210" :marker-end="$clicks >= 3 ? 'url(#goap-head-on)' : 'url(#goap-head)'" />
    <path class="goap-hl" pathLength="1" d="M820,364 L960,210" />
    <text x="880" y="262" text-anchor="end">judgeDraft · 3</text>
  </g>
  <g class="goap-edge" :class="{ 'is-on': $clicks >= 4, 'is-off': $clicks >= 5 }">
    <path class="goap-base" d="M1125,170 Q1265,170 1265,358" :marker-end="$clicks === 4 ? 'url(#goap-head-on)' : 'url(#goap-head)'" />
    <path class="goap-hl" pathLength="1" d="M1125,170 Q1265,170 1265,358" />
    <text x="1250" y="150" text-anchor="middle">acceptDraft · 0</text>
    <text class="goap-cond" x="1250" y="116" text-anchor="middle">{{ $clicks >= 5 ? 'pre DRAFT_ACCEPTED = false' : 'pre DRAFT_ACCEPTED' }}</text>
  </g>
  <g class="goap-edge goap-edge-replan" :class="{ 'is-on': $clicks >= 5 }">
    <path class="goap-base" d="M1040,206 Q1040,400 1144,400" :marker-end="$clicks >= 5 ? 'url(#goap-head-on)' : 'url(#goap-head)'" />
    <path class="goap-hl" pathLength="1" d="M1040,206 Q1040,400 1144,400" />
    <text x="975" y="470" text-anchor="middle">reviseDraft · 3</text>
    <text class="goap-cond" x="975" y="502" text-anchor="middle">pre DRAFT_REJECTED</text>
  </g>
  <!-- nodes: world states on the blackboard, the post of one action is the pre of the next -->
  <g class="goap-node goap-start is-on">
    <rect x="10" y="364" width="230" height="72" rx="14" />
    <text x="125" y="407" text-anchor="middle">TripReviewRequest</text>
  </g>
  <g class="goap-node" :class="{ 'is-on': $clicks >= 1 }">
    <rect x="315" y="134" width="230" height="72" rx="14" />
    <text x="430" y="177" text-anchor="middle">TripDossier</text>
  </g>
  <g class="goap-node" :class="{ 'is-on': $clicks >= 2 }">
    <rect x="625" y="364" width="230" height="72" rx="14" />
    <text x="740" y="407" text-anchor="middle">ReviewOutcome</text>
  </g>
  <g class="goap-node" :class="{ 'is-on': $clicks >= 3 }">
    <rect x="895" y="134" width="230" height="72" rx="14" />
    <text x="1010" y="177" text-anchor="middle">Judgement</text>
  </g>
  <g class="goap-node goap-goal" :class="{ 'is-on': $clicks >= 4 }">
    <rect x="1150" y="364" width="230" height="72" rx="14" />
    <text x="1265" y="407" text-anchor="middle">EmbabelReview</text>
  </g>
  <text class="goap-tally" :class="{ 'is-on': $clicks >= 4 }" x="1265" y="480" text-anchor="middle">{{ $clicks >= 5 ? 'replanned · cost 10' : 'plan · cost 7' }}</text>
  <!-- legend: the grammar of the graph, said once -->
  <g class="goap-legend">
    <rect x="10" y="540" width="44" height="30" rx="7" />
    <text x="66" y="563">world state = pre / post</text>
    <line x1="380" y1="555" x2="440" y2="555" marker-end="url(#goap-head)" />
    <text x="456" y="563">action · cost</text>
  </g>
</svg>

<!--
- The graph the planner searches
- Boxes = world states, types on the blackboard
- Arrows = actions, pre -> post
- Numbers = cost
- draftWithTools - illustrative
  - Spring AI / Koog way: model fetches via tools
  - expensive: 8
- [click] Start at the request, two ways out
  - loadDossier 1 vs draftWithTools 8 - cheap one first
- [click] Via dossier to ReviewOutcome: 1 + 3 = 4
  - cheaper than 8, tool route dropped
  - arithmetic, not vibes
- [click] judgeDraft -> Judgement
- [click] acceptDraft free - plan total 7
  - found before any action runs
  - deterministic, logged
- [click] Judge rejects
  - DRAFT_ACCEPTED false, acceptDraft impossible
  - replan: reviseDraft, total 10
- No hand-written if - conditions decide the edges
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-brackets-curly />type safety</div>

# The caller names an input and a goal type, never the steps

```kotlin no-compile
data class TripReviewRequest(val tripId: Long)
data class TripDossier(val trip: TripSnapshot, val costs: CostBreakdown)
data class EmbabelReview(val outcome: ReviewOutcome, val revised: Boolean)
```

```kotlin no-compile
AgentInvocation.create<EmbabelReview>(agentPlatform)
    .invoke(TripReviewRequest(tripId))
    .outcome
```

<!--
- Three data classes - agent vocabulary
  - typed blackboard
  - actions read inputs, write results
- Invocation: TripReviewRequest in, EmbabelReview wanted
  - no step names, no graph, no loop
- .outcome unwraps ReviewOutcome for TripReviewer
-->

---
class: code-slide
---

# An `@Action` can be plain code that never calls a model

```kotlin no-compile
@Action
fun loadDossier(request: TripReviewRequest): TripDossier =
    TripDossier(facts.trip(request.tripId), facts.costs(request.tripId))
```

<!--
- First plan step, no LLM
- TripReviewRequest in, TripDossier out - enough for the planner
- Loading data: deterministic, fast, free
  - Spring AI / Koog: model fetches via tool
  - here: code fetches upfront, model gets the full dossier
- Agent inside the system - just call the service
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-brackets-curly />type safety</div>

# A model-backed action asks for an object, not a string

```kotlin no-compile
@Action
fun draftReview(dossier: TripDossier, context: OperationContext): ReviewOutcome =
    context.ai()
        .withDefaultLlm()
        .withSystemPrompt(Prompts.REVIEWER)
        .createObject("Review this trip: $dossier")
```

<!--
- TripDossier in, ReviewOutcome out - runs after loadDossier
- createObject - return type defines the output
- Prompt runner resembles ChatClient - not a coincidence
- Tools line omitted - next slide
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-plugs-connected />integration</div>

# Tools are granted per model call, not per agent

```kotlin no-compile
@Component
class EmbabelTravelTools(private val facts: TravelFacts) {
    @LlmTool(description = "List all company travel policies")
    fun listPolicies(): List<PolicyText> = facts.policies()
}
```

```kotlin no-compile
context.ai().withDefaultLlm().withToolObject(tools)  // draftReview
context.ai().withDefaultLlm()                        // judgeDraft
```

<!--
- Tool looks like everywhere else
  - bean, method, description, TravelFacts
  - schema from the signature
- Per call, not per agent
  - reviewer gets tools, judge does not
- Why
  - least privilege by default
  - fewer tools - fewer wrong calls, fewer tokens
- Trip + costs in the dossier - only listPolicies needed
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-books />context</div>

# A domain object can bring its own tools

```kotlin no-compile
data class TripDossier(val trip: TripSnapshot, val costs: CostBreakdown) {

    @LlmTool(description = "Hotels above a nightly limit in EUR")
    fun hotelsAbove(limit: Double): List<HotelLine> =
        trip.hotels.filter { it.nightlyRate > limit }
}
```

```kotlin no-compile
context.ai().withDefaultLlm().withToolObject(dossier)
```

<!--
- Not in the demo
- Domain integrated context engineering
  - behavior on the domain object, callable while in play
  - no mirror tools class
- hotelsAbove
  - precise question, exact answer
  - no arithmetic or filtering by the model
- Rod's warning
  - only safe behavior - models get trigger happy
  - mutations belong in code actions
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-lifebuoy />reliability</div>

# A side effect waits for a type only a human can produce

```kotlin no-compile
@Action
fun askTraveler(proposal: RebookingProposal): ApprovedRebooking =
    WaitFor.confirmation(ApprovedRebooking(proposal), "Rebook ${proposal.hotel}?")

@Action
fun rebook(approved: ApprovedRebooking): HotelBooking =
    bookings.rebook(approved.proposal)
```

<!--
- Not in the demo - the fixing agent
- No undo registry - prevent instead of compensate
- rebook needs ApprovedRebooking
  - only producer: human confirmation
  - type = permission, model cannot skip it
- WaitFor parks the process in WAITING
  - embabel-agent-starter-jdbc: snapshot in the DB
  - restart or other node resumes, no waiting thread
- @Action knobs: readOnly, canRerun
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-books />context</div>

# The memory is the typed blackboard, not a growing chat transcript

```kotlin no-compile
context.ai().withDefaultLlm().withToolObject(tools)
    .withToolLoopTransformers(
        SlidingWindowTransformer(20),
        ToolResultTruncatingTransformer(4_000),
    )
```

<!--
- Embabel docs quote
  - "does not maintain a conversation thread to do its core work"
  - "context compression is not required for most tasks"
- Fresh prompt per action from blackboard objects
  - judge sees TripDossier + ReviewOutcome, not 40 messages
  - blackboard = working memory = my data classes
- Transformers - for long tool loops
  - sliding window keeps the system message
  - truncated tool results
  - own transformer for summarizing
- Chatbots: explicit Conversation, chat store module
-->

---
class: graphic-slide
---

<div class="pillar-tag"><ph-chart-line-up />observability</div>

# Spring AI traces the model calls, Embabel adds the plan around them

<div class="trace">
  <div v-click="1" class="trace-span reveal" style="--width: 100%"><span>agent TripReviewAgent</span><i></i></div>
  <div v-click="1" class="trace-span trace-tool reveal" style="--depth: 1; --start: 0%; --width: 2%"><span>action loadDossier</span><i></i></div>
  <div v-click="1" class="trace-span reveal" style="--depth: 1; --start: 3%; --width: 50%"><span>action draftReview</span><i></i></div>
  <div class="trace-span" style="--depth: 2; --start: 4%; --width: 22%"><span>gen_ai.client.operation</span><i></i></div>
  <div v-click="1" class="trace-span trace-tool reveal" style="--depth: 2; --start: 27%; --width: 2%"><span>tool listPolicies</span><i></i></div>
  <div class="trace-span" style="--depth: 2; --start: 30%; --width: 22%"><span>gen_ai.client.operation</span><i></i></div>
  <div v-click="1" class="trace-span reveal" style="--depth: 1; --start: 54%; --width: 44%"><span>action judgeDraft</span><i></i></div>
  <div class="trace-span" style="--depth: 2; --start: 55%; --width: 42%"><span>gen_ai.client.operation</span><i></i></div>
  <div v-click="1" class="trace-span trace-tool reveal" style="--depth: 1; --start: 98%; --width: 1%"><span>action acceptDraft</span><i></i></div>
</div>

<!--
- Spring AI metrics carry over
- Zero config
  - every model call = ChatModel observation
  - model, tokens, latency, token usage metric
- [click] embabel-agent-starter-observability
  - agent span, action spans, tools, replanning
  - which step burned time and tokens
  - @Tracked nests own methods
  - MDC: run id, agent, action
- Exporters: Zipkin, OTLP, Langfuse
- Labels simplified, timings illustrative
-->

---
class: code-slide code-dense
---

# The judge declares both outcomes, conditions decide which one is true

```kotlin no-compile {1-2|4-8}
@Action(post = [DRAFT_ACCEPTED, DRAFT_REJECTED])
fun judgeDraft(dossier: TripDossier, draft: ReviewOutcome, ...): Judgement

@Condition(name = DRAFT_ACCEPTED)
fun draftAccepted(judgement: Judgement): Boolean = judgement.accepted

@Condition(name = DRAFT_REJECTED)
fun draftRejected(judgement: Judgement): Boolean = !judgement.accepted
```

<!--
- Types cannot express accepted vs rejected - both Judgement
- post: one of these may change after judging
  - every goal condition must be some action's post
  - learned the hard way building the demo
- [click] Plain predicates on Judgement
  - exclusive by construction
  - side-effect free, unit tested
- Judge body: createObject without tools, omitted
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-lifebuoy />reliability</div>

# Two goals finish the review, the true condition picks one

```kotlin no-compile {1-4|6-9}
@AchievesGoal(description = "The trip review was confirmed by the judge")
@Action(pre = [DRAFT_ACCEPTED])
fun acceptDraft(draft: ReviewOutcome, judgement: Judgement): EmbabelReview =
    EmbabelReview(draft, revised = false)

@AchievesGoal(description = "The trip review was corrected after the judge's feedback")
@Action(pre = [DRAFT_REJECTED])
fun reviseDraft(dossier: TripDossier, draft: ReviewOutcome, judgement: Judgement, ...) =
    EmbabelReview(/* one more model call with the feedback */, revised = true)
```

<!--
- Both produce EmbabelReview - the goal type
- Accepted: draft as is, no extra call
- [click] Rejected: one more reviewer call with feedback
- Difference to the others
  - revises once, no re-judge
  - Spring AI, Koog loop up to twice
  - loop possible via @State or a new draft to judge
- Descriptions tell platform and other agents what the goal means
-->

---
class: graphic-slide
---

# Nobody wrote this order down, the planner derived it from the types

<div class="flow flow-5 flow-compact">
  <div class="flow-node"><ph-airplane-tilt /><span><code>TripReviewRequest</code></span></div>
  <div class="flow-arrow"><span>loadDossier</span></div>
  <div class="flow-node"><ph-folder-open /><span><code>TripDossier</code></span></div>
  <div class="flow-arrow"><span>draftReview</span></div>
  <div class="flow-node"><ph-file-text /><span><code>ReviewOutcome</code></span></div>
</div>

<div v-click class="reveal">
  <div class="flow flow-5 flow-turn">
    <div></div>
    <div></div>
    <div></div>
    <div></div>
    <div class="flow-turn-cell"><div class="flow-arrow flow-arrow-down"></div><span>judgeDraft</span></div>
  </div>
  <div class="flow flow-5 flow-compact flow-return">
    <div class="flow-node"><ph-arrow-circle-right /><span><code>.outcome</code></span></div>
    <div class="flow-arrow flow-arrow-back"></div>
    <div class="flow-node"><ph-flag-checkered /><span><code>EmbabelReview</code></span></div>
    <div class="flow-arrow flow-arrow-back"><span>accept | revise</span></div>
    <div class="flow-node"><ph-gavel /><span><code>Judgement</code></span></div>
  </div>
</div>

<!--
- Plan found for our invocation
  - arrows = @Action, boxes = blackboard types
  - sequence never written down
- [click] Branch = the conditions
  - accepted - acceptDraft, rejected - reviseDraft
- Why compelling
  - new action, e.g. exchange rates - used without rewiring
  - refactor a type - compiler flags misfit actions
- Trade-off
  - flow not in one place - read from the planner log
  - opposite of the Spring AI repeat
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-brackets-curly />type safety</div>

# Shared actions live in a component, not inside one agent

```kotlin no-compile
@EmbabelComponent
class TripActions(val facts: TravelFacts, val rates: ExchangeRates) {

    @Action fun loadDossier(request: TripReviewRequest): TripDossier = ...

    @Action(readOnly = true)
    fun inEuro(dossier: TripDossier): EuroCosts = rates.convert(dossier.costs)
}
```

<!--
- Not in the demo - how I would grow it
- loadDossier moved into a component
- New: inEuro for foreign hotel prices
- Component is not an agent
  - contributes actions, goals, conditions
  - Spring stereotype, normal injection
- Only types
  - any agent needing a TripDossier can plan with it
  - no calls by name
- Utility planner: most valuable next action across the platform
- Every id -> rich object method = an action waiting to happen
-->

---
class: code-slide code-dense
---

# A whole agent becomes one step of the next agent

```kotlin no-compile
@Agent(description = "Settles expense claims after a trip")
class ExpenseClaimAgent(private val tripReview: TripReviewAgent) {

    @Action
    fun reviewTrip(claim: ExpenseClaim): EmbabelReview =
        RunSubagent.fromAnnotatedInstance(tripReview, EmbabelReview::class.java)

    @AchievesGoal(description = "The expense claim was settled")
    @Action
    fun settle(claim: ExpenseClaim, review: EmbabelReview): Settlement = TODO()
}
```

<!--
- Not in the demo - next agent for the travel team
- Agents are beans - inject one into another
- RunSubagent
  - full trip review = one action
  - result = just another blackboard type
- New agent adds only settling
- Scales out: goals via MCP or A2A
- Plumbing simplified - claim must provide TripReviewRequest
-->

---
class: graphic-slide
---

# Every new agent needs fewer new actions

<div class="reuse">
  <div class="reuse-row">
    <span class="reuse-agent">trip review</span>
    <i class="reuse-new">loadDossier</i>
    <i class="reuse-new">draftReview</i>
    <i class="reuse-new">judgeDraft</i>
    <i class="reuse-new">accept | revise</i>
  </div>
  <div v-click class="reuse-row reveal">
    <span class="reuse-agent">expense claim</span>
    <i>loadDossier</i>
    <i>trip review</i>
    <i class="reuse-new">matchReceipts</i>
    <i class="reuse-new">settle</i>
  </div>
  <div v-click class="reuse-row reveal">
    <span class="reuse-agent">visa check</span>
    <i>loadDossier</i>
    <i>trip review</i>
    <i class="reuse-new">checkVisa</i>
  </div>
</div>

<!--
- The Embabel takeaway
- First agent: four new actions
  - solid = new, outlined = reused
- [click] Second: two of four reused
  - dossier + trip review as subagent
- [click] Third, visa check: one new action
  - planner wires an unwritten flow
- Why Embabel
  - connected by types, not drawn edges or loops
  - Spring AI: copy the loop, graph: redraw edges
- Agents 2 and 3 illustrative
-->
