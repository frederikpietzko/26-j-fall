---
class: graphic-slide
---

# Agents in production need what Java has delivered for decades

<div class="pillar-grid">
  <div class="pillar"><ph-books /><span>context</span></div>
  <div class="pillar"><ph-lifebuoy /><span>reliability</span></div>
  <div class="pillar"><ph-brackets-curly /><span>type safety</span></div>
  <div class="pillar"><ph-lightning /><span>performance</span></div>
  <div class="pillar"><ph-chart-line-up /><span>observability</span></div>
  <div class="pillar"><ph-plugs-connected /><span>integration</span></div>
</div>

<!--
- Demo agent: prompt + API key
- Production agent needs more
  - context - data, domain model, business rules
  - reliability - retries, timeouts, transactions
  - type safety - output checked by the compiler
  - performance - concurrent network calls
  - observability - what the agent did and why
  - integration - systems we already run
- Nothing new - Java's job for decades
-->

---
class: graphic-slide
---

# Java has kept the enterprise running for 30 years

<div class="timeline">
  <div class="timeline-point"><logos-java /><strong>1996</strong><span>Java 1.0</span></div>
  <div class="timeline-point"><logos-spring-icon /><strong>2004</strong><span>Spring 1.0</span></div>
  <div class="timeline-point"><ph-function /><strong>2014</strong><span>Java 8 lambdas</span></div>
  <div class="timeline-point"><ph-lightning /><strong>2023</strong><span>virtual threads</span></div>
  <div v-click class="timeline-point timeline-next reveal"><ph-robot /><strong>2025</strong><span>JVM agent frameworks</span></div>
</div>

<!--
- 30 years of resilient enterprise systems
  - banks, insurers, airlines, logistics
- Evolved without breaking users
  - Spring, Java 8 lambdas, Java 21 virtual threads
- [click] Agent frameworks
  - Spring AI, Koog, Embabel - all within the last year or so
- Agents = next chapter, not a different book
-->

---
layout: intro
class: agenda
---

# What we'll cover

1. Why python was first and why Java will last
2. The demo: reviewing business trips
3. Spring AI
4. Embabel
5. Koog
