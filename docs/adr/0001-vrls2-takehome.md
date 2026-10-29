# ADR 0001 — VRLS2 take-home

## Context

VRLS2 extends the supplied Angular/Spring Boot starter, using four CSVs as the source of truth. [Issue #1](https://github.com/engjw/lta_assignment/issues/1) implements strict ingestion and date display. [Issue #3](https://github.com/engjw/lta_assignment/issues/3) implements safe, responsive vehicle-detail popups. [Issue #5](https://github.com/engjw/lta_assignment/issues/5) selects sorting and separate date filters for test-first development; server-side search remains a proposal.

## Conflicts found in §5

- **R-1.4 / R-2.1:** choose full vehicle UUID ascending, then registration (`start_date`) descending within each UUID. This prioritises stable UUID grouping over global newest-first order; it does not fully satisfy both requirements.
- **R-1.1 / R-1.7 / R-4.4 versus R-3.2 usability:** choose a list with date filters instead of implementing the calendar. Scale raises a usability concern, not a logical contradiction. Calendar requirements remain unmet by this deliberate scope decision.
- **R-1.8 / R-3.1 / R-3.2 / R-3.3:** defer the proposed API search payload and 2,000-record limit. Server-side queries would depart from R-3.1. A hard result cap could hide history (R-4.5); full-dataset querying, 50-record pages (R-1.3) and continuation beyond any batch limit need a decision. The two-second target needs measurement, not a unit-test claim.

## Data model

Keep validated events in memory: eight CSV fields plus source-derived category, with no database. Vehicle UUID identifies a vehicle, not an event; preserve repeated-UUID events (R-4.2, R-4.3, R-4.5). Strict source dates (`DD/MM/YYYY`) become ISO API dates and `DD MMM YYYY` display dates (R-2.3); time stays `HH:mm:ss`.

Independent `currentpath/<vehicle-uuid>` popups match the clicked session snapshot against validated API events. Refresh restores a matching selection; otherwise show a sole event or chooser. Close preserves list state; details support 1280×720 and narrow screens.

Issue #5 tests specify optional, inclusive `start_date >= Start date` and `end_date <= End date`, combined with AND and the status filter. These are separate date fields, not bounds on `start_date + time`; clearing restores the supplied events. Tests cover supplied list records only: current API pagination leaves full-history sorting/filtering unresolved.

## Security & threat model

Treat CSV input and browser storage as untrusted. Validate schema, presence, types, UUIDs, lengths and status/source consistency before serving (R-6.1). Reject malformed rows without rewriting input; log source, row, field and reason without raw values. Use Angular safe bindings, no `innerHTML` or sanitiser bypasses; detach popup openers (R-6.2).

**R-6.3 stance:** anonymous API access is for local development. Before production, require login, enforce admin permission per request and refuse startup without authentication configuration; this closure plan is unimplemented. Secrets belong in environment configuration (R-6.5). Accepted-record provenance/ingestion times (R-6.4) and controlled error envelopes (R-6.6) remain outstanding. Compromised-server protection is outside this local input-validation threat model.

## One explicit trade-off

Give up the monthly calendar to focus on list-based date selection at scale. This simplifies the chosen interface but sacrifices the required visual overview; date filters are not equivalent calendar compliance.

## Open questions

- Confirm the 100/50-character brand/type bounds and source-correction ownership.
- Future detail links: database record UUIDs, or vehicle UUID URLs with API-backed event tabs? Exact-event sharing still needs a stable event identifier.
- Approve the calendar departure, define complete API search/pagination and the performance benchmark, and choose the production identity provider/admin roles.
