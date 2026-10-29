# ADR 0001 — VRLS2 take-home

## Context

VRLS2 builds on the supplied Angular/Spring Boot starter and four source CSVs. [Issue #1](https://github.com/engjw/lta_assignment/issues/1) implements validated ingestion and date display. [Issue #3](https://github.com/engjw/lta_assignment/issues/3) implements safe, responsive detail popups. [Issue #5](https://github.com/engjw/lta_assignment/issues/5) has client date filters/sorting; submitted server search is the next test-first step.

## Conflicts found in §5

- **R-1.4 / R-2.1:** full UUID ascending, then registration (`start_date`) descending within each UUID. UUID grouping takes precedence over global newest-first order.
- **R-1.1 / R-1.7 / R-4.4 versus R-3.2 usability:** choose list-based date selection instead of the calendar. This addresses a usability concern, not a logical contradiction; calendar requirements remain unmet.
- **R-1.8 / R-3.1 / R-3.2 / R-3.3:** search all valid events server-side, then order and cap at 2,000. This departs from R-3.1; status filters and 50-row paging stay local (R-1.3). Show total matches and a refine-search notice when truncated. No continuation is planned; broad searches limit history access (R-4.5). The two-second target needs measurement.

## Data model

Keep all valid events in memory: eight CSV fields plus source-derived category. UUID identifies a vehicle, not an event; preserve repeated UUIDs (R-4.2–R-4.3). Source dates (`DD/MM/YYYY`) become ISO API dates and `DD MMM YYYY` display dates (R-2.3); time stays `HH:mm:ss`.

Independent `currentpath/<vehicle-uuid>` popups match session selections against API events. Refresh restores the match; otherwise show a sole event or chooser. Close preserves list state; details support desktop/narrow screens.

Planned `POST /api/vehicles/search`: optional `query`, `startDate`, `endDate`, `type`; response `records`, `totalMatches`, `truncated`. Trim query text; match case-insensitive UUID prefixes or brand substrings, exact type, and inclusive `start_date >= startDate` / `end_date <= endDate`, combined with AND. Dates exclude `time`. Search precedes the cap; source records remain unchanged.

Show no results before Search: fields on row one, Search row two, status filters below. Blank Search returns up to 2,000 events; edits wait for resubmission. Filter returned events before paging; both statuses includes both. New searches reset the page. These behaviors are tests only so far.

## Security & threat model

Treat CSV, query input and browser storage as untrusted. Validate CSV schema, presence, types, UUIDs, lengths and status/source consistency (R-6.1); reject without rewriting, logging source/row/reason. Planned search validates ISO dates and 100/50-character query/type bounds; the server enforces the cap. Use Angular safe bindings, no HTML/sanitiser bypasses; detach popup openers (R-6.2).

**R-6.3 stance:** anonymous API access is for local development. Before production, require login, enforce admin permission per request and refuse startup without authentication configuration; this closure plan is unimplemented. Secrets belong in environment configuration (R-6.5). Accepted-record provenance/ingestion times (R-6.4) and controlled error envelopes (R-6.6) remain outstanding. Compromised-server protection is outside this local input-validation threat model.

## One explicit trade-off

Bound each search response to 2,000 events for manageable browser work. Users must narrow larger searches; identical matching criteria can still leave events unreachable. This is a declared history-access limitation, not full R-4.5 compliance.

## Open questions

- Confirm the 100/50-character brand/type bounds and source-correction ownership.
- Future detail links: database record UUIDs, or vehicle UUID URLs with API-backed event tabs? Exact-event sharing still needs a stable event identifier.
- Confirm calendar/history-access departures, define the performance benchmark, and choose production identity/admin roles.
