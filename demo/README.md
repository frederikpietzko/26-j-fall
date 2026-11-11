# Travel policy demo - three agent frameworks, one Spring Boot service

A single Kotlin + Spring Boot 4 microservice that manages business **trips** (hotel, flight and
train bookings) and plain-text **travel policies**. When a trip is submitted, a review job runs
asynchronously and lets one of three agent frameworks check the trip against the policies:

| Agent       | Showcases                                                                        | Code                                  |
|-------------|----------------------------------------------------------------------------------|---------------------------------------|
| `SPRING_AI` | `ChatClient.Builder`, `SimpleLoggerAdvisor` + custom `CallAdvisor`, `@Tool`       | `review/springai/SpringAiTripReviewer.kt` |
| `EMBABEL`   | `@Agent`, `@Action` with `pre`/`post`, `@Condition`, `@AchievesGoal`, `@LlmTool`  | `review/embabel/EmbabelTripReviewer.kt`   |
| `KOOG`      | graph `strategy { }` with `subgraphWithTask`, `subgraphWithVerification` and plain nodes/edges, `ToolSet`, koog-spring-ai bridge | `review/koog/KoogTripReviewer.kt`         |

All three use the **judge pattern** (a second LLM call audits the draft review and can send it
back for revision) and all three talk to the model through the **same Spring AI `ChatModel`** -
Spring AI 2.1's `OpenAiResponsesChatModel` (`spring.ai.openai.chat.api=responses`).

## Versions

Spring Boot 4.1.1, Spring AI 2.1.0-M1 (Responses API), Embabel 1.5.2, Koog 1.3.0
(`koog-spring-ai-v2-starter-model-chat`), Kotlin 2.4.20, Java 25.

## Run

Credentials are read from the slides repo's `../.env` (`OPENAI_BASE_URL`, `OPENAI_API_KEY`).

```bash
./gradlew bootRun
```

Two trips are seeded: trip `1` complies with every policy, trip `2` breaks several.
See `requests.http` for the full API. The short version:

```bash
curl -X POST "localhost:8080/api/trips/2/submit?agent=KOOG"   # 202, review runs async
curl localhost:8080/api/trips/2/reviews                       # verdict + violations
```

A trip that came back `NEEDS_CHANGES` can be edited and submitted again - with any agent.

## Tests

```bash
./gradlew test                                   # everything, no network needed
./gradlew test --tests '*IT'                     # integration tests only
```

All three frameworks call the LLM through a Spring AI `ChatModel`, so the tests replace exactly that one
boundary with `ScriptedChatModel` (`src/test/kotlin/.../fakes`). It answers judge prompts and reviewer prompts
from separate scripts and records every prompt, so tests can assert on what the agents sent. When Koog's
subgraphs offer their `finalize_task_result` finish tool, a scripted JSON answer is sent as that tool call.

- **Unit tests** (no Spring context, in-memory repositories): `TripServiceTest`, `TravelFactsTest`,
  `SpringAiTripReviewerTest` (judge loop, revision limit, `@Tool` callbacks), `TripReviewEvaluatorTest`
  (the judge as a Spring AI `Evaluator`), `KoogTripReviewerTest`
  (graph strategy with subgraphs, real tool execution through the Spring AI bridge, judge isolation), `TripReviewAgentTest`
  (Embabel actions and conditions are plain methods).
- **Integration tests** (`@IntegrationTest` = `@SpringBootTest` + MockMvc + the fake model): `TripApiIT` covers
  the REST CRUD, `ReviewFlowIT` submits over HTTP and waits for the async review of **each** agent - including
  Embabel's planner - plus the judge-rejects-draft path, locking and failure handling.

## Gotchas found while building it

- Embabel's `@Agent` scanning is annotated `@Profile("!test")`: with a Spring profile called `test`
  no agent gets deployed. The integration tests therefore use the profile `it`.
- koog-spring-ai's auto-configuration only orders itself after `OpenAiChatAutoConfiguration`,
  not after Spring AI 2.1's `OpenAiResponsesChatAutoConfiguration`, so `KoogConfig` wires the
  `SpringAiLLMClient` explicitly.
- The Spring AI bridge does not forward response schemas. Koog's `subgraphWithTask` and
  `subgraphWithVerification` sidestep that: the typed result comes back as the arguments of a finish tool call.
- `subgraphWithVerification` runs inside the caller's conversation. To keep the judge isolated it is wrapped
  in a `subgraph(freshHistory = true)` and swaps in the judge system prompt.
- The nodes inside subgraphs count against `maxAgentIterations`, so the limit is 200 instead of 50.
- Embabel's GOAP planner needs every condition a goal depends on to appear as some action's
  `post` condition - hence `judgeDraft` declares both `draftAccepted` and `draftRejected`.
