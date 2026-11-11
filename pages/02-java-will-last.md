---
layout: intro
class: section-intro
---

# Why Python was first

## and why Java will last

<!--
- Not bashing Python - it earned its place
- Why Python won round one
- Why the JVM fits now
- Six tiles from the intro - one tag per slide
-->

---
class: graphic-slide
---

# AI started in the lab, and the lab spoke Python

<div class="logo-hub">
  <div class="hub-center">
    <logos-python />
    <span>research &amp; quick prototyping</span>
  </div>
  <div class="hub-ring">
    <div v-click class="logo-tile reveal"><logos-numpy /><span>numpy</span></div>
    <div v-click class="logo-tile reveal"><logos-pandas-icon /><span>pandas</span></div>
    <div v-click class="logo-tile reveal"><simple-icons-scikitlearn class="brand-scikit" /><span>scikit-learn</span></div>
  </div>
</div>

<!--
- AI = research topic, done by scientists
- Python: notebooks, no build, no types, throwaway prototypes
- Early network effects
  - [click] numpy
  - [click] pandas
  - [click] scikit-learn
  - every paper in Python - next paper too
- Won on ecosystem, not language
- Research code runs once on a laptop
  - no reliability, type safety, observability needed
-->

---
class: graphic-slide
---

# Researchers joined companies and brought Python with them

<div class="flow">
  <div class="flow-node"><ph-flask /><span>research lab</span></div>
  <div class="flow-arrow"><logos-python /></div>
  <div class="flow-node"><ph-buildings /><span>companies</span></div>
</div>

<div v-click class="logo-row reveal">
  <div class="logo-tile"><simple-icons-langchain /><span>LangChain</span></div>
  <div class="logo-tile"><simple-icons-langgraph /><span>LangGraph</span></div>
</div>

<!--
- Researchers hired into industry, kept Python
- [click] First agent frameworks in Python
  - LangChain, LangGraph
  - tutorials, courses - all Python
- "AI means Python" becomes common wisdom
-->

---
class: graphic-slide
---

# Python was the glue, the heavy lifting always ran in C and C++

<div class="stack">
  <div class="stack-layer stack-thin"><logos-python /><span>the API you call</span></div>
  <div class="stack-layer stack-heavy">
    <div class="stack-logos"><logos-c /><logos-c-plusplus /><logos-nvidia /></div>
    <span>the code that actually does the math</span>
  </div>
</div>

<!--
- numpy, PyTorch, TensorFlow - C, C++, CUDA underneath
- Python = thin convenience layer
- Top layer always replaceable
-->

---
class: graphic-slide
---

# LLMs moved the heavy lifting into the datacenter

<div class="flow">
  <div class="flow-node"><ph-laptop /><span>your application</span></div>
  <div class="flow-arrow flow-arrow-labelled"><span>HTTPS</span></div>
  <div class="flow-node flow-node-wide">
    <div class="stack-logos">
      <simple-icons-openai />
      <simple-icons-anthropic />
      <logos-google-gemini-icon />
      <logos-mistral-ai-icon />
    </div>
    <span>thousands of GPUs</span>
  </div>
</div>

<!--
- LLMs mostly too big to run locally
  - datacenters, thousands of GPUs, plain APIs
- Heavy lifting no longer on our machine
  - "Python has the math libs" argument gone
- Left on our side: classic enterprise software
  - calling services, validation, failure handling, 24/7
  - Java's 30 year track record
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-brackets-curly />type safety</div>

# What travels over the wire is JSON and JSON Schema

```json
{
  "name": "create_invoice",
  "parameters": {
    "type": "object",
    "properties": {
      "customer": { "type": "string" },
      "total":    { "type": "number" },
      "dueDate":  { "type": "string", "format": "date" }
    },
    "required": ["customer", "total", "dueDate"]
  }
}
```

<!--
- Everything is JSON - requests, responses, tool definitions
- Structured output + tool calling = JSON Schema
- Typical tool definition = a type definition in JSON
- Python: dicts, validation bolted on
- Java: typed JSON for 20 years
-->

---
class: graphic-slide
---

<div class="pillar-tag"><ph-lightning />performance</div>

# An agent is mostly concurrent network IO, and the JVM is built for it

<div class="flow flow-fan">
  <div class="flow-node flow-fan-source"><logos-java /><span>your agent</span></div>
  <div class="flow-arrow flow-arrow-both"><span>JSON</span></div>
  <div class="flow-node"><ph-cloud /><span>LLM API</span></div>
  <div v-click="1" class="flow-arrow flow-arrow-both reveal"><span>JSON</span></div>
  <div v-click="1" class="flow-node reveal"><ph-plugs-connected /><span>tools &amp; services</span></div>
</div>

<!--
- Network IO + JSON handling
  - call model, parse, call tool, repeat
- [click] Tools = more HTTP calls
- JVM strengths: HTTP clients, connection pools, virtual threads
- Performance = waiting well
  - thousands of parallel runs blocked on LLM calls
  - virtual threads, no reactive gymnastics
  - one JVM vs worker processes and the GIL
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-brackets-curly />type safety</div>

# Jackson turns a record into the schema and the answer back into a record

```java no-compile
record Invoice(String customer, BigDecimal total, LocalDate dueDate) {}

JsonSchema schema = schemaGenerator.generateSchema(Invoice.class);

Invoice invoice = mapper.readValue(llmResponse, Invoice.class);
```

<!--
- Same invoice, as a record
- Jackson: type -> JSON Schema
- Jackson: response -> type
- Compiler catches mismatches
- Malformed answer fails at the boundary
- What you see is what runs
-->

---
class: graphic-slide
---

<div class="pillar-tag"><ph-books />context</div>

# The context an agent needs already lives in JVM systems

<div class="logo-hub">
  <div class="hub-center">
    <logos-java />
    <span>the data, the domain model, the business rules</span>
  </div>
  <div class="hub-ring">
    <div class="logo-tile"><ph-bank /><span>banking</span></div>
    <div class="logo-tile"><ph-shield-check /><span>insurance</span></div>
    <div class="logo-tile"><ph-truck /><span>logistics</span></div>
    <div class="logo-tile"><ph-factory /><span>manufacturing</span></div>
    <div class="logo-tile"><ph-first-aid-kit /><span>healthcare</span></div>
  </div>
</div>

<!--
- Most enterprise systems in Java
  - banking, insurance, logistics, manufacturing, healthcare
- Agent only as good as its context
  - model knows the world, not your customers, contracts, rules
  - already modelled in these systems
- Most automation potential lives here
-->

---
class: graphic-slide
---

<div class="pillar-tag"><ph-plugs-connected />integration</div>

# Put the agent inside the system, not next to it

<div class="compare">
  <div class="compare-card compare-bad">
    <div class="compare-flow">
      <logos-java /><ph-arrow-right /><simple-icons-modelcontextprotocol /><ph-arrow-right /><logos-python />
    </div>
    <span>an MCP server and a Python agent per legacy system</span>
  </div>
  <div v-click class="compare-card compare-good reveal">
    <div class="compare-flow">
      <logos-java /><ph-plus /><ph-robot />
    </div>
    <span>the agent lives in the Java project you already have</span>
  </div>
</div>

<!--
- Python way
  - MCP server per legacy system
  - separate Python agent
  - two stacks, deployments, teams
- [click] Agent inside the Java project
  - same build, tests, ops, people
- Integration for free
  - tools = plain method calls
  - same transactions, security, DB connections
  - no second domain model
-->

---
class: graphic-slide
---

<div class="pillar-tag"><ph-lifebuoy />reliability<ph-chart-line-up />observability</div>

# A production agent needs the same safety net as any other service

<div class="logo-hub">
  <div class="hub-center">
    <ph-robot />
    <span>your agent</span>
  </div>
  <div class="hub-ring">
    <div class="logo-tile"><ph-arrows-counter-clockwise /><span>retries</span></div>
    <div class="logo-tile"><ph-lock-key /><span>security</span></div>
    <div v-click class="logo-tile reveal"><logos-opentelemetry-icon /><span>OpenTelemetry</span></div>
    <div v-click class="logo-tile reveal"><logos-prometheus /><span>Prometheus</span></div>
    <div v-click class="logo-tile reveal"><logos-grafana /><span>Grafana</span></div>
  </div>
</div>

<!--
- LLM calls: slow, flaky, expensive - unreliable remote service
- Reliability
  - retries, timeouts, circuit breakers, rate limits - Resilience4j, Spring Retry
  - security + transactions around tool calls
- Observability
  - [click] model + tool calls as trace spans
  - [click] tokens + latency as metrics
  - [click] existing ops dashboards
- No observability, no production
- Spring AI built in, Embabel + Koog add spans
-->

---
class: graphic-slide
---

# The big JVM vendors are building best-in-class agent frameworks

<div class="logo-row logo-row-large">
  <div v-click class="logo-tile reveal"><logos-spring-icon /><span>Spring AI</span></div>
  <div v-click class="logo-tile reveal"><logos-kotlin-icon /><span>Koog</span></div>
  <div v-click class="logo-tile logo-tile-wordmark reveal">
    <img class="only-light" src="/logos/embabel-light.png" alt="Embabel" />
    <img class="only-dark" src="/logos/embabel-dark.png" alt="Embabel" />
  </div>
</div>

<!--
- Not a hobby scene anymore
- [click] Spring AI - Spring team, Broadcom
- [click] Koog - JetBrains, Kotlin first
- [click] Embabel - Rod Johnson, creator of Spring
- The three for the rest of the talk
-->

---
class: graphic-slide
---

# Most AI projects fail because they automate the wrong thing

<div class="compare">
  <div class="compare-card compare-bad">
    <ph-chats-circle class="compare-icon" />
    <span>replacing human interaction</span>
  </div>
  <div v-click class="compare-card compare-good reveal">
    <ph-arrows-clockwise class="compare-icon" />
    <span>boring, repetitive, time-consuming processes</span>
  </div>
</div>

<!--
- Most AI projects fail
- Automating human interaction = usually wrong
  - chatbots replacing support, sales, advisors
- [click] Boring, repetitive, time-consuming processes
- Legacy processes already in Java
- Need all six tiles from the start
-->
