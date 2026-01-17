# Orders Monthly Aggregation (Camel + Embedded Artemis JMS)

This application reads an `orders.csv` file, aggregates order totals by month, produces an XML report, then demonstrates **two different channel types** using JMS:

1. **Point-to-Point (P2P)** via a **JMS Queue**
2. **Publish-Subscribe (Pub/Sub)** via a **JMS Topic**

The JMS broker is **embedded Apache ActiveMQ Artemis**, started inside the same JVM as the application (no external broker needed).

---

## What the app does (high-level)

### Step 1 — File → Orders
- Input file: `data/in/orders.csv`
- Camel `file:` consumer reads the CSV
- Camel `bindy` unmarshals each row into an `Order` Java object

### Step 2 — Aggregation by Month
- Each `Order` computes its month (e.g., `"2024-11-01" → "November"`)
- The route aggregates orders by month using a Camel `aggregate()`
- For each month, it builds a `MonthlySummary` object (month + total amount)
- A second aggregation collects all monthly summaries into one root collection for XML output

### Step 3 — Produce XML
- The aggregated object is marshalled to XML (JAXB), resulting in:
  ```xml
  <monthlySummaries>
      <monthlySummary month="December" total="5840.0"/>
      ...
  </monthlySummaries>

## Step 4 — Channel communications

### Point-to-Point (P2P) using a JMS Queue

- The XML report is sent to a **JMS Queue**:`jms:queue:monthlySummaries`

**What happens in the app:**
- The aggregation route finishes and sends the XML as the message body into the queue.
- The queue acts as a buffer/decoupling channel between producers and consumers.

---

### Publish-Subscribe (Pub/Sub) using a JMS Topic

- A separate route consumes the message from the queue and publishes it to a **JMS Topic**:
    - `jms:topic:monthlySummaries.events`

**What happens in the app:**
- The "bridge" route reads from the queue and republishes to the topic.
- Two independent subscribers listen to the topic and both receive the same XML.

Subscribers implemented:
- `topic-subscriber-audit` → writes the XML to `data/out/audit/`
- `topic-subscriber-notify` → writes the XML to `data/out/notify/`

---
#### Input `data/in/orders.csv`
#### Output `data/out/audit/` and `data/out/notify/`
