---
layout: intro
class: section-intro
---

# Spring AI

## the ChatClient you already know how to wire

<!--
- Control flow: I own it
  - plain Kotlin loop
-->

---
class: code-slide
---

# One builder call configures the reviewer once

```kotlin no-compile
private val reviewer: ChatClient = builder.clone()
    .defaultSystem(Prompts.REVIEWER)
    .defaultTools(tools)
    .defaultAdvisors(SimpleLoggerAdvisor(), TimingAdvisor())
    .build()
```

<!--
- Auto-configured ChatClient.Builder, injected
  - clone() - judge builds its own client
- Three defaults
  - system prompt
  - tools - next slide
  - advisors - interceptors around every call
- Nothing sent yet - config, like RestClient
- SpringAiTripReviewer.kt
-->

---
class: code-slide graphic-slide
---

<div class="pillar-tag"><ph-plugs-connected />integration</div>

# A tool is a Spring bean method with a description

```kotlin no-compile
@Component
class SpringAiTravelTools(private val facts: TravelFacts) {

    @Tool(description = "Calculate hotel nights and total costs of a trip")
    fun calculateCosts(
        @ToolParam(description = "The trip id") tripId: Long,
    ): CostBreakdown = facts.costs(tripId)
}
```

<div v-click class="flow reveal">
  <div class="flow-node"><ph-sparkle /><span>model</span></div>
  <div class="flow-arrow flow-arrow-both"><span>tool call / result</span></div>
  <div class="flow-node"><logos-spring-icon /><span>our bean</span></div>
</div>

<!--
- Also getTrip, listPolicies
- JSON Schema from the method signature
  - description + ToolParam -> schema
  - CostBreakdown -> JSON
- [click] Model never runs our code
  - asks for a tool by name + args
  - Spring AI calls the method, returns the result
  - 2.1: loop is an advisor - ToolCallingAdvisor
- Integration for free
  - normal bean - transactions, security, repositories
  - TravelFacts shared with the REST API
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-brackets-curly />type safety</div>

# The request reads like the question and returns a Kotlin type

```kotlin no-compile
var draft = reviewer.prompt()
    .user(Prompts.review(tripId))
    .call()
    .entity<ReviewOutcome>()
```

<!--
- prompt, user, call - reads like the question
- entity<ReviewOutcome>
  - schema from the class
  - Jackson back into the data class
  - Jackson slide, as one call
- Malformed answer fails at the boundary
- var - judge may replace the draft
-->

---
class: graphic-slide
---

# Every call passes through a chain of advisors before it reaches the model

<svg class="advisor-chain" viewBox="0 0 1240 600" role="img" aria-label="Spring AI advisor chain">
  <defs>
    <marker id="advisor-arrow" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
      <path class="advisor-arrow-head" d="M0,0 L10,5 L0,10 z" />
    </marker>
  </defs>
  <rect class="advisor-layer" x="170" y="10" width="660" height="400" rx="22" />
  <text x="500" y="48" text-anchor="middle">SimpleLoggerAdvisor</text>
  <rect class="advisor-layer" x="230" y="66" width="540" height="328" rx="20" />
  <text x="500" y="104" text-anchor="middle">TimingAdvisor</text>
  <rect class="advisor-layer advisor-layer-tools" x="290" y="122" width="420" height="256" rx="18" />
  <text x="500" y="160" text-anchor="middle">ToolCallingAdvisor</text>
  <line class="advisor-flow" x1="140" y1="210" x2="924" y2="210" marker-end="url(#advisor-arrow)" />
  <line class="advisor-flow" x1="928" y1="350" x2="142" y2="350" marker-end="url(#advisor-arrow)" />
  <text class="advisor-caption" x="500" y="245" text-anchor="middle">before advice</text>
  <text class="advisor-caption" x="500" y="328" text-anchor="middle">after advice</text>
  <circle class="advisor-io" cx="80" cy="210" r="56" />
  <text class="advisor-io-label" x="80" y="218" text-anchor="middle">Prompt</text>
  <circle class="advisor-io" cx="80" cy="350" r="56" />
  <text class="advisor-io-label advisor-io-label-long" x="80" y="357" text-anchor="middle">Response</text>
  <rect class="advisor-model" x="930" y="180" width="260" height="200" rx="16" />
  <text x="1060" y="272" text-anchor="middle">ChatModel</text>
  <text class="advisor-caption" x="1060" y="306" text-anchor="middle">LLM provider</text>
  <g v-click class="reveal">
    <line class="advisor-loop" x1="470" y1="382" x2="470" y2="494" marker-end="url(#advisor-arrow)" />
    <line class="advisor-loop" x1="530" y1="498" x2="530" y2="386" marker-end="url(#advisor-arrow)" />
    <text class="advisor-caption" x="458" y="432" text-anchor="end">tool call</text>
    <text class="advisor-caption" x="542" y="432">result</text>
    <rect class="advisor-domain" x="170" y="440" width="960" height="150" rx="22" />
    <text x="196" y="482">our application</text>
    <rect class="advisor-tools" x="380" y="500" width="240" height="70" rx="14" />
    <text x="500" y="544" text-anchor="middle">@Tool beans</text>
    <line class="advisor-link" x1="624" y1="535" x2="684" y2="535" marker-end="url(#advisor-arrow)" />
    <rect class="advisor-node" x="690" y="500" width="200" height="70" rx="14" />
    <text x="790" y="544" text-anchor="middle">TravelFacts</text>
    <line class="advisor-link" x1="936" y1="535" x2="896" y2="535" marker-end="url(#advisor-arrow)" />
    <rect class="advisor-node" x="940" y="500" width="170" height="70" rx="14" />
    <text x="1025" y="544" text-anchor="middle">REST API</text>
  </g>
</svg>

<!--
- Picture behind the builder
- Round trip
  - prompt in - before advice in every advisor
  - ChatModel outside the chain - remote provider
  - response out - after advice
  - nested like servlet filters
- Outer two: SimpleLoggerAdvisor, TimingAdvisor
  - order from getOrder, not builder position
- Inner: ToolCallingAdvisor
  - since 2.1 the tool loop = just an advisor
- [click] Tool call into our application
  - tools = domain logic, not AI stack
  - @Tool beans -> TravelFacts, same as REST API
  - loops until final answer
  - TimingAdvisor measures the whole loop
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-chart-line-up />observability</div>

# An advisor wraps every call like a servlet filter

```kotlin no-compile
class TimingAdvisor : CallAdvisor {
    override fun adviseCall(
        request: ChatClientRequest,
        chain: CallAdvisorChain,
    ): ChatClientResponse {
        val started = System.currentTimeMillis()
        val response = chain.nextCall(request)
        val usage = response.chatResponse()?.metadata?.usage
        log.info("took {} ms, tokens: {}",
            System.currentTimeMillis() - started, usage?.totalTokens)
        return response
    }
}
```

<!--
- Servlet filter, interceptor, aspect
  - before, chain, after
- chain.nextCall - everything downstream incl. the model
- Real uses: session memory, RAG, guardrails, logging, tool loop
- SimpleLoggerAdvisor - same shape
- Log line shortened
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-books />context</div>

# Memory is a session now: advisors over an append-only event log

```kotlin no-compile
val chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(SessionAdvisors.builder(sessionService)
        .compaction(TurnCountTrigger(20),
            WindowCompactionStrategy.builder().lastTurns(10).build())
        .build())
    .build()

chatClient.prompt()
    .advisors(SessionAdvisors.session("trip-$tripId"))
    .user("Why was my trip rejected?")
    .call()
    .content()
```

<!--
- Not in the demo - first traveler question: why?
- Spring AI 2.1 - Session replaces ChatMemory
  - ChatMemory deprecated, removed in 3.0
- Session = append-only event log
  - every message an event, nothing overwritten
- SessionAdvisors - two advisors, same before / after shape
  - turn advisor: create session, compact
  - memory advisor: load view per model call, append after
- Session id on every call - one per trip
  - created on first use
- Compaction instead of a message window
  - 20 turns -> keep last 10
  - cut on turn boundaries, tool calls stay with their turn
  - log kept, only the view shrinks
  - or summarize: ChatModelSummarizer
- Storage
  - in memory, JDBC: Postgres, MySQL, MariaDB, H2
  - JDBC starter auto-configures SessionService
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-lifebuoy />reliability</div>

# Spring AI already ships a judge: the `FactCheckingEvaluator`

```kotlin no-compile
val judge: Evaluator = FactCheckingEvaluator.builder()
    .chatClientBuilder(builder)
    .build()

val evaluation = judge.evaluate(
    EvaluationRequest(tripFacts.map(::Document), draft.toString()))

if (!evaluation.isPass) revise(draft)
```

<!--
- LLM as judge built in - Evaluator interface
  - testing chapter of the docs, not test only
- Context docs + response under test
  - trip facts as Documents, draft as response
- isPass - evaluator decides, code reacts
- Two built in
  - FactCheckingEvaluator - claims backed by docs, hallucination check
  - RelevancyEvaluator - fits question + context, RAG check
- Not enough for us
  - yes / no only, no feedback
  - checks facts, not policies
- Illustration - demo uses its own evaluator
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-lifebuoy />reliability</div>

# The judge implements Spring AI's own `Evaluator` contract

```kotlin no-compile
@Component
class TripReviewEvaluator(builder: ChatClient.Builder) : Evaluator {
    private val judge = builder.clone()
        .defaultSystem(Prompts.JUDGE)
        .build()

    override fun evaluate(request: EvaluationRequest): EvaluationResponse {
        val judgement = judge.prompt()
            .user(Prompts.judge(request.dataList, request.responseContent))
            .call()
            .entity<Judgement>()
        return EvaluationResponse(
            judgement.accepted, judgement.feedback, emptyMap())
    }
}
```

<!--
- Same interface, my prompt, my types
- EvaluationRequest in, EvaluationResponse out
  - dataList: trip, costs, policies as Documents
  - responseContent: the draft
  - response: pass + feedback
- Inside: ChatClient without tools, typed Judgement
  - user line shortened
- Why
  - reviewer depends on Evaluator, not a prompt - swappable judge
  - reusable in integration tests on recorded reviews
- TripReviewEvaluator.kt
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-lifebuoy />reliability</div>

# The whole agent loop is a Kotlin `repeat`

```kotlin no-compile {all|7-9|10-13|all}
override fun review(tripId: Long): ReviewOutcome {
    var draft = reviewer.prompt()
        .user(Prompts.review(tripId))
        .call()
        .entity<ReviewOutcome>()

    repeat(MAX_REVISIONS) {
        val evaluation = judge.evaluate(evaluationRequest(tripId, draft))
        if (evaluation.isPass) return draft
        draft = reviewer.prompt()
            .user(Prompts.revise(tripId, draft, evaluation.feedback))
            .call()
            .entity<ReviewOutcome>()
    }
    return draft
}
```

<!--
- No agent abstraction
  - ordinary Kotlin, readable by everyone
- [click] Judge - injected Evaluator
  - facts as documents, draft as response
  - isPass - return the draft
- [click] Failed - one more reviewer call with feedback
  - MAX_REVISIONS = 2 - no runaway token burn
- [click] What you see is what runs
  - debugger, unit tests, stack traces
  - tests fake only the ChatModel
- Price: every branch and loop is mine as the flow grows
-->
