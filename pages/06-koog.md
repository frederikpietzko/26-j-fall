---
layout: intro
class: section-intro
---

# Koog

## the agent is a graph you can read

<!--
- JetBrains, Kotlin first, Java API
- Born in JetBrains products, open source 2025, 1.x stable
- JVM, Android, iOS, JS, Wasm
- Control flow: I draw the graph, Koog walks it
  - opposite of Embabel - every edge written down
- Same demo, ReviewOutcome, judge
- Graph by hand first, then library subgraphs
- LangGraph, but typed
-->

---
class: graphic-slide
clicks: 2
---

# The same review becomes a graph of nodes and edges

<svg class="koog-graph" viewBox="0 40 1400 420" role="img" aria-label="Koog strategy graph with a tool loop and a revision loop">
  <defs>
    <linearGradient id="kg1-gradient" gradientUnits="userSpaceOnUse" x1="0" y1="0" x2="1400" y2="0">
      <stop offset="0" stop-color="var(--fundamentals-purple)" />
      <stop offset="1" stop-color="var(--fundamentals-pink)" />
    </linearGradient>
    <marker id="kg1-head" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
      <path class="kg-head" d="M0,0 L10,5 L0,10 z" />
    </marker>
    <marker id="kg1-head-on" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
      <path class="kg-head kg-head-on" d="M0,0 L10,5 L0,10 z" />
    </marker>
  </defs>
  <!-- edges: one edge(...) call each, grouped by the loop they belong to -->
  <g class="kg-edge">
    <path class="kg-base" d="M130,265 L176,265" marker-end="url(#kg1-head)" />
  </g>
  <!-- tool loop -->
  <g class="kg-edge" :class="{ 'is-on': $clicks === 1 }">
    <path class="kg-base" d="M330,233 Q330,132 446,132" :marker-end="$clicks === 1 ? 'url(#kg1-head-on)' : 'url(#kg1-head)'" />
    <path class="kg-hl" stroke="url(#kg1-gradient)" pathLength="1" d="M330,233 Q330,132 446,132" />
    <text x="318" y="185" text-anchor="end">tool call</text>
  </g>
  <g class="kg-edge" :class="{ 'is-on': $clicks === 1 }">
    <path class="kg-base" d="M650,132 L726,132" :marker-end="$clicks === 1 ? 'url(#kg1-head-on)' : 'url(#kg1-head)'" />
    <path class="kg-hl" stroke="url(#kg1-gradient)" pathLength="1" d="M650,132 L726,132" />
  </g>
  <g class="kg-edge" :class="{ 'is-on': $clicks === 1 }">
    <path class="kg-base" d="M820,100 C820,40 560,40 560,96" :marker-end="$clicks === 1 ? 'url(#kg1-head-on)' : 'url(#kg1-head)'" />
    <path class="kg-hl" stroke="url(#kg1-gradient)" pathLength="1" d="M820,100 C820,40 560,40 560,96" />
    <text x="690" y="78" text-anchor="middle">tool call</text>
  </g>
  <!-- done researching -->
  <g class="kg-edge" :class="{ 'is-on': $clicks === 2 }">
    <path class="kg-base" d="M380,265 L726,265" :marker-end="$clicks === 2 ? 'url(#kg1-head-on)' : 'url(#kg1-head)'" />
    <path class="kg-hl" stroke="url(#kg1-gradient)" pathLength="1" d="M380,265 L726,265" />
    <text x="555" y="252" text-anchor="middle">text</text>
  </g>
  <g class="kg-edge">
    <path class="kg-base" d="M865,164 L865,229" marker-end="url(#kg1-head)" />
    <text x="880" y="204">text</text>
  </g>
  <!-- revision loop -->
  <g class="kg-edge" :class="{ 'is-on': $clicks === 2 }">
    <path class="kg-base" d="M1000,265 L1066,265" :marker-end="$clicks === 2 ? 'url(#kg1-head-on)' : 'url(#kg1-head)'" />
    <path class="kg-hl" stroke="url(#kg1-gradient)" pathLength="1" d="M1000,265 L1066,265" />
  </g>
  <g class="kg-edge">
    <path class="kg-base" d="M1220,265 L1266,265" marker-end="url(#kg1-head)" />
    <text x="1245" y="222" text-anchor="middle">accepted</text>
  </g>
  <g class="kg-edge" :class="{ 'is-on': $clicks === 2 }">
    <path class="kg-base" d="M1145,297 C1145,420 280,420 280,301" :marker-end="$clicks === 2 ? 'url(#kg1-head-on)' : 'url(#kg1-head)'" />
    <path class="kg-hl" stroke="url(#kg1-gradient)" pathLength="1" d="M1145,297 C1145,420 280,420 280,301" />
    <text x="712" y="428" text-anchor="middle">rejected</text>
  </g>
  <!-- nodes: every box is one node declaration in the strategy -->
  <g class="kg-node kg-terminal">
    <rect x="10" y="233" width="120" height="64" rx="32" />
    <text x="70" y="272" text-anchor="middle">start</text>
  </g>
  <g class="kg-node">
    <rect x="180" y="233" width="200" height="64" rx="14" />
    <text x="280" y="272" text-anchor="middle">review</text>
  </g>
  <g class="kg-node">
    <rect x="450" y="100" width="200" height="64" rx="14" />
    <text x="550" y="139" text-anchor="middle">run-tools</text>
  </g>
  <g class="kg-node">
    <rect x="730" y="100" width="270" height="64" rx="14" />
    <text x="865" y="139" text-anchor="middle">send-tool-results</text>
  </g>
  <g class="kg-node">
    <rect x="730" y="233" width="270" height="64" rx="14" />
    <text x="865" y="272" text-anchor="middle">structure-draft</text>
  </g>
  <g class="kg-node">
    <rect x="1070" y="233" width="150" height="64" rx="14" />
    <text x="1145" y="272" text-anchor="middle">judge</text>
  </g>
  <g class="kg-node kg-terminal">
    <rect x="1270" y="233" width="120" height="64" rx="32" />
    <text x="1330" y="272" text-anchor="middle">finish</text>
  </g>
</svg>

<!--
- Every box = one line of Kotlin
- Left to right
  - review asks the model
  - tool call - run-tools - send-tool-results
  - repeats until a text answer
  - structure-draft -> ReviewOutcome
  - judge accepts or sends back
- Two cycles
  - [click] tool loop - Spring AI: hidden in an advisor
  - [click] revision loop - Spring AI: a repeat
  - here: visible edges
- Long form first - shows what subgraphs do later
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-plugs-connected />integration</div>

# Tools live in a `ToolRegistry` the agent is built with

```kotlin no-compile
class KoogTravelTools(private val facts: TravelFacts) : ToolSet {
    @Tool
    @LLMDescription("Load a submitted trip with all bookings")
    fun getTrip(@LLMDescription("The trip id") tripId: Long): TripSnapshot =
        facts.trip(tripId)
}
```

```kotlin no-compile
val tools = ToolRegistry {
    tools(KoogTravelTools(facts))
}

AIAgent(promptExecutor = executor, strategy = strategy,
    agentConfig = config, toolRegistry = tools)
```

<!--
- Like @Tool, @LlmTool - methods + descriptions
  - ToolSet groups them, also listPolicies, calculateCosts
  - same TravelFacts
- Registry = everything the agent may call
  - Kotlin DSL, same style as strategy + prompt
  - Koog executes, model only asks
  - combinable with plus - shared registries
- Alternatives
  - class based tools, typed Args
  - built-in tools: talk to user, end run
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-brackets-curly />type safety</div>

# Every node is a typed value declared up front

```kotlin no-compile
strategy<String, ReviewOutcome>("trip-review") {
    var revisions = 0

    val review by nodeLLMRequest("review")
    val runTools by nodeExecuteTools("run-tools")
    val sendToolResults by nodeLLMSendToolResults("send-tool-results")
    val draft by nodeLLMRequestStructured<ReviewOutcome>("structure-draft")
    // edges on the next slide
}
```

<!--
- strategy<String, ReviewOutcome> - prompt -> our type
- Four library nodes
  - LLM request, execute tools, send results, structured request
- Typed input / output per node
  - mismatched types do not compile
- revisions - local var, captured by the strategy
- Judge later, as a subgraph
-->

---
class: code-slide code-dense
---

# Four edges are the whole tool loop

```kotlin no-compile {1-4|6-7}
edge(nodeStart forwardTo review)
edge(review forwardTo runTools onToolCalls { true })
edge(runTools forwardTo sendToolResults)
edge(sendToolResults forwardTo runTools onToolCalls { true })

edge(review forwardTo draft onTextMessage { true } transformed { STRUCTURE })
edge(sendToolResults forwardTo draft onTextMessage { true } transformed { STRUCTURE })
```

<!--
- First four lines = tool loop
  - onToolCalls - model asked for a tool
  - results back to the model, may ask again
- [click] Done researching
  - text answer - structure-draft
  - transformed: "return final review as structured output"
  - parsed into ReviewOutcome
- Control flow as data - trace, checkpoint, visualize
- Every agent needs this loop
-->

---
class: code-slide code-dense
---

# Koog ships the whole tool loop as one `subgraphWithTask`

````md magic-move
```kotlin no-compile
val review by nodeLLMRequest("review")
val runTools by nodeExecuteTools("run-tools")
val sendToolResults by nodeLLMSendToolResults("send-tool-results")
val draft by nodeLLMRequestStructured<ReviewOutcome>("structure-draft")

edge(review forwardTo runTools onToolCalls { true })
edge(runTools forwardTo sendToolResults)
edge(sendToolResults forwardTo runTools onToolCalls { true })
edge(review forwardTo draft onTextMessage { true } transformed { STRUCTURE })
edge(sendToolResults forwardTo draft onTextMessage { true } transformed { STRUCTURE })
```

```kotlin no-compile
val review by subgraphWithTask<String, ReviewOutcome>(name = "review") { task ->
    task
}
```
````

<!--
- What we just built: four nodes, five edges
- [click] What the demo ships
  - same loop as a library subgraph
  - String in, ReviewOutcome out - typed node
  - lambda: input -> task, here input = task
- Inside
  - finish tool finalize_task_result with ReviewOutcome schema
  - forced tool calls until the finish tool
  - finish tool call = structured output, no extra request
- Principle moved into the library
- Gotcha: inner nodes count against maxAgentIterations - raised to 200
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-brackets-curly />type safety</div>

# The judge is a `subgraphWithVerification` that never sees the reviewer's chat

```kotlin no-compile {1-3|4-10}
val judge by subgraph<ReviewOutcome, CriticResult<ReviewOutcome>>(
    "judge", freshHistory = true,
) {
    val verify by subgraphWithVerification<ReviewOutcome>(tools = emptyList()) { draft ->
        llm.writeSession {
            clearHistory()
            appendPrompt { system(Prompts.JUDGE) }
        }
        Prompts.judge(facts.trip(tripId), facts.policies(), facts.costs(tripId), draft)
    }
    nodeStart then verify then nodeFinish
}
```

<!--
- Judge pattern shipped: subgraphWithVerification
  - draft in, CriticResult out - successful, feedback, draft
  - same finish tool trick, verdict instead of review
- freshHistory = true
  - default: runs inside the reviewer's chat
  - fresh: empty chat, reviewer's chat restored afterwards
- [click] No tools, judge prompt, facts + draft
  - independent opinion, no self-agreement
  - subgraphs nest - still just a node
- suspend everywhere - coroutines all the way
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-lifebuoy />reliability</div>

# The revision loop stays a plain node and two edges

```kotlin no-compile {1-4|6|7-10}
val revise by node<CriticResult<ReviewOutcome>, String>("revise") { critique ->
    revisions++
    Prompts.revise(tripId, critique.input, critique.feedback)
}

nodeStart then review then judge
edge(judge forwardTo nodeFinish
    onCondition { it.successful || revisions >= MAX_REVISIONS } transformed { it.input })
edge(judge forwardTo revise onCondition { !it.successful })
revise then review
```

<!--
- Revision policy is mine, not library
  - plain node: verdict -> next reviewer prompt
- [click] then = unconditional edge
- [click] Finish on accept or MAX_REVISIONS (2)
  - else revise, back into review
  - reviewer keeps its chat - knows its last draft
- Gotcha: at the cap both conditions true
  - first matching edge wins - order matters
- Safety net: maxAgentIterations = 200
- Lines wrapped, KoogTripReviewer.kt
-->

---
class: graphic-slide
---

# Two library subgraphs and one node of mine are the whole agent

<svg class="koog-graph" viewBox="0 160 1400 410" role="img" aria-label="Koog agent built from two library subgraphs and one custom node">
  <defs>
    <linearGradient id="kg2-gradient" gradientUnits="userSpaceOnUse" x1="0" y1="0" x2="1400" y2="0">
      <stop offset="0" stop-color="var(--fundamentals-purple)" />
      <stop offset="1" stop-color="var(--fundamentals-pink)" />
    </linearGradient>
    <marker id="kg2-head" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
      <path class="kg-head" d="M0,0 L10,5 L0,10 z" />
    </marker>
    <marker id="kg2-head-on" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
      <path class="kg-head kg-head-on" d="M0,0 L10,5 L0,10 z" />
    </marker>
  </defs>
  <!-- frames: the two library subgraphs, each is a single node in my graph -->
  <g class="kg-frame">
    <rect x="180" y="175" width="440" height="220" rx="18" />
    <text x="202" y="208">subgraphWithTask</text>
  </g>
  <g class="kg-frame">
    <rect x="700" y="175" width="400" height="220" rx="18" />
    <text x="722" y="208">subgraphWithVerification</text>
  </g>
  <!-- edges: library edges stay muted, the edges of my policy are drawn in -->
  <g class="kg-edge">
    <path class="kg-base" d="M130,285 L176,285" marker-end="url(#kg2-head)" />
  </g>
  <g class="kg-edge">
    <path class="kg-base" d="M380,274 L446,274" marker-end="url(#kg2-head)" />
    <path class="kg-base" d="M450,296 L384,296" marker-end="url(#kg2-head)" />
    <text x="415" y="345" text-anchor="middle">tool calls</text>
  </g>
  <g class="kg-edge">
    <path class="kg-base" d="M620,285 L696,285" marker-end="url(#kg2-head)" />
  </g>
  <g class="kg-edge is-on">
    <path class="kg-base" d="M1100,285 L1246,285" marker-end="url(#kg2-head-on)" />
    <path class="kg-hl" stroke="url(#kg2-gradient)" pathLength="1" d="M1100,285 L1246,285" />
    <text x="1173" y="268" text-anchor="middle">accepted</text>
  </g>
  <g class="kg-edge is-on">
    <path class="kg-base" d="M900,395 Q900,480 784,480" marker-end="url(#kg2-head-on)" />
    <path class="kg-hl" stroke="url(#kg2-gradient)" pathLength="1" d="M900,395 Q900,480 784,480" />
    <text x="915" y="462">rejected</text>
  </g>
  <g class="kg-edge is-on">
    <path class="kg-base" d="M580,480 Q400,480 400,399" marker-end="url(#kg2-head-on)" />
    <path class="kg-hl" stroke="url(#kg2-gradient)" pathLength="1" d="M580,480 Q400,480 400,399" />
  </g>
  <!-- nodes -->
  <g class="kg-node kg-terminal">
    <rect x="10" y="253" width="120" height="64" rx="32" />
    <text x="70" y="292" text-anchor="middle">start</text>
  </g>
  <g class="kg-node">
    <rect x="210" y="253" width="170" height="64" rx="14" />
    <text x="295" y="292" text-anchor="middle">review</text>
  </g>
  <g class="kg-node">
    <rect x="450" y="253" width="140" height="64" rx="14" />
    <text x="520" y="292" text-anchor="middle">tools</text>
  </g>
  <g class="kg-node">
    <rect x="750" y="253" width="300" height="64" rx="14" />
    <text x="900" y="292" text-anchor="middle">judge</text>
  </g>
  <text class="kg-note" x="900" y="352" text-anchor="middle">freshHistory = true</text>
  <g class="kg-node kg-mine">
    <rect x="580" y="448" width="200" height="64" rx="14" />
    <text x="680" y="487" text-anchor="middle">revise</text>
  </g>
  <g class="kg-node kg-terminal">
    <rect x="1250" y="253" width="130" height="64" rx="32" />
    <text x="1315" y="292" text-anchor="middle">finish</text>
  </g>
  <!-- legend: library versus mine, said once -->
  <g class="kg-legend">
    <g class="kg-frame"><rect x="10" y="535" width="44" height="30" rx="7" /></g>
    <text x="66" y="558">Koog library subgraph</text>
    <g class="kg-node kg-mine"><rect x="380" y="535" width="44" height="30" rx="7" /></g>
    <text x="436" y="558">my node and edges</text>
  </g>
</svg>

<!--
- Same agent as the first map - same behavior, same tests
- Tool loop + structuring = one subgraph
- Judge = subgraph with fresh history
- revise + its edges = my policy
- Library subgraphs first, single nodes where control is needed
-->

---
class: code-slide code-dense graphic-slide
---

<div class="pillar-tag"><ph-plugs-connected />integration</div>

# Koog runs on the same Spring AI `ChatModel`

```kotlin no-compile
@Bean
fun koogPromptExecutor(chatModel: ChatModel): PromptExecutor = MultiLLMPromptExecutor(
    SpringAiLLMClient.builder()
        .chatModel(chatModel)
        .provider(LLMProvider.OpenAI)
        .build()
)
```

<div v-click class="flow flow-5 reveal">
  <div class="flow-node"><logos-kotlin-icon /><span>AIAgent</span></div>
  <div class="flow-arrow"></div>
  <div class="flow-node"><ph-plugs-connected /><span>SpringAiLLMClient</span></div>
  <div class="flow-arrow"></div>
  <div class="flow-node"><logos-spring-icon /><span>ChatModel</span></div>
</div>

<!--
- Koog clients: OpenAI, Anthropic, Google, Ollama, ...
- Spring shop: no second credentials, HTTP clients
  - koog-spring-ai wraps Boot's ChatModel
- [click] Koog: graph + tools, Spring AI: model calls only
- Gotcha: auto-config missed the Spring AI 2.1 Responses API model
  - one bean wired by hand, normally the starter
- No response schema forwarding - JSON format in the prompt
-->

---
class: graphic-slide
---

<div class="pillar-tag"><ph-chart-line-up />observability</div>

# Spring AI traces the model calls, Koog adds the graph around them

<div class="trace">
  <div v-click="1" class="trace-span reveal" style="--width: 100%"><span>agent trip-review</span><i></i></div>
  <div v-click="1" class="trace-span reveal" style="--depth: 1; --start: 0%; --width: 63%"><span>subgraph review</span><i></i></div>
  <div class="trace-span" style="--depth: 2; --start: 1%; --width: 22%"><span>gen_ai.client.operation</span><i></i></div>
  <div v-click="1" class="trace-span trace-tool reveal" style="--depth: 2; --start: 25%; --width: 3%"><span>tool getTrip</span><i></i></div>
  <div class="trace-span" style="--depth: 2; --start: 30%; --width: 32%"><span>gen_ai.client.operation</span><i></i></div>
  <div v-click="1" class="trace-span reveal" style="--depth: 1; --start: 64%; --width: 35%"><span>subgraph judge</span><i></i></div>
  <div class="trace-span" style="--depth: 2; --start: 65%; --width: 33%"><span>gen_ai.client.operation</span><i></i></div>
</div>

<!--
- Same picture as Embabel - on purpose
- Free via the bridge
  - ChatModel instrumented with Micrometer
  - model, latency, tokens, token usage metric
- Spring AI blind spots
  - Koog runs tools - no Spring AI tool span
  - no idea which node made the call
- [click] Koog OpenTelemetry feature
  - spans for agent run, subgraphs, nodes, tools
  - subgraphs still visible in the trace
  - OTel GenAI semantic conventions
- Labels simplified, timings illustrative
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-chart-line-up />observability</div>

# One `install` sends the whole graph to Langfuse

```kotlin no-compile
AIAgent(
    promptExecutor = executor,
    strategy = reviewStrategy(tripId),
    agentConfig = agentConfig,
    toolRegistry = tools,
) {
    install(OpenTelemetry) {
        setServiceInfo("trip-review", "1.0.0")
        addLangfuseExporter()
    }
}
```

<!--
- Features installed like Ktor plugins
  - OpenTelemetry, tracing, event handlers, persistence, chat memory
- addLangfuseExporter - ships with Koog
  - LANGFUSE env vars: host, keys
  - prompts, responses, tokens, cost per node
  - also W&B Weave, Datadog, any OTLP
- Prompts masked unless verbose
- Both OpenTelemetry - same backend as the submitting REST call
- Not in the demo repo - first line before production
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-books />context</div>

# Chat memory is a feature I install, compression is a node I place

```kotlin no-compile
install(ChatMemory) {
    chatHistoryProvider = JdbcChatHistory(dataSource)
    windowSize(20)
}
```

```kotlin no-compile
val compress by nodeLLMCompressHistory<ReceivedToolResults>(
    strategy = HistoryCompressionStrategy.FromLastNMessages(5),
)
```

<!--
- Two problems, two tools
- ChatMemory - across runs
  - load / store history per session id
  - agent.run(input, sessionId) - "why was my trip rejected?"
  - JdbcChatHistory mine - ChatHistoryProvider: store + load
  - windowSize, filterMessages built in
- History compression - within one run
  - a node - I pick where
  - after tool bursts, before the context fills
  - strategies: TLDR, last N, chunked, fact retrieval
- LongTermMemory - vector store, RAG recall
- Review agent needs none - chat agents do
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-lifebuoy />reliability</div>

# Every tool can register its undo, a rollback runs them backwards

```kotlin no-compile
install(Persistence) {
    storage = JVMFilePersistenceStorageProvider(checkpoints)
    enableAutomaticPersistence = true
    rollbackToolRegistry = RollbackToolRegistry {
        registerRollback(bookings::addHotel, bookings::removeHotel)
    }
}
```

```kotlin no-compile
context.persistence().rollbackToCheckpoint(checkpointId, context)
```

<!--
- Next agent fixes the trip - swaps the 520 EUR hotel
  - side effects, bad run = broken state
- Persistence checkpoints
  - automatic: after every node - history, node, storage
  - crash - resume from the checkpoint
- RollbackToolRegistry - unique to Koog
  - Spring AI: own undo log, Embabel: human approval first
  - each side-effect tool names its undo tool
  - rollback: restore state, undo tools in reverse
- Undo is my code - Koog guarantees call + order
- bookings, checkpoints illustrative - review agent read only
-->

---
class: graphic-slide
---

# The graph is one strategy, Koog lets every agent pick its own

<div class="pillar-grid strategy-grid">
  <div class="pillar pillar-current"><ph-graph /><span>graph strategy</span></div>
  <div v-click class="pillar reveal"><ph-repeat /><span>functional loop</span></div>
  <div v-click class="pillar reveal"><ph-arrows-clockwise /><span>ReAct and chat</span></div>
  <div v-click class="pillar reveal"><ph-sparkle /><span>LLM planner</span></div>
  <div v-click class="pillar reveal"><ph-path /><span>GOAP planner</span></div>
  <div class="pillar pillar-current"><ph-tree-structure /><span>subgraphs</span></div>
</div>

<!--
- Map only, all documented
- Graph + subgraphs - today, highlighted
- [click] Functional strategy
  - suspend lambda, while loop
  - Spring AI repeat inside Koog
- [click] Predefined: single run, ReAct, chat
- [click] LLM planners
  - model writes and revises the plan, optional critic
  - flexible, only as good as the model
- [click] GOAP planner
  - preconditions, effects, A*
  - Embabel idea as a Koog strategy
- Subgraphs nest - big agents from small tested pieces
- Wrap-up
  - Spring AI: I write the loop
  - Embabel: planner finds it
  - Koog: I draw it, or pick any of the above
  - all three: inside our Java service, same ChatModel, same traces
-->
