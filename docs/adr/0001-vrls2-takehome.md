# ADR 0001 — VRLS2 take-home

## Context

VRLS2 retains the supplied Angular/Spring Boot starter and four source CSVs. [Issue #1](https://github.com/engjw/lta_assignment/issues/1) adds validated ingestion/date display; [#3](https://github.com/engjw/lta_assignment/issues/3) adds safe, responsive detail popups. [Issue #5](https://github.com/engjw/lta_assignment/issues/5) adds submitted server searches capped at 2,000 events with local status filters/paging. [Issue #7](https://github.com/engjw/lta_assignment/issues/7) specifies auditability and security regression tests; its new controls are planned, not implemented.

## Conflicts found in §5

- **R-1.4 / R-2.1:** full UUID ascending takes precedence; registration (`start_date`) descending breaks ties within each UUID.
- **R-1.1 / R-1.7 / R-4.4 versus R-3.2 usability:** use list date selection instead of a calendar. This is a usability choice, not a logical contradiction; calendar requirements remain unmet.
- **R-1.8 / R-3.1 / R-3.2 / R-3.3:** search all valid events server-side, sort, then cap at 2,000. This departs from R-3.1; status filtering and 50-row paging remain local (R-1.3). Show total matches and a refine-search notice when truncated. No continuation; history access is limited (R-4.5). The two-second target needs measurement.

## Data model

Keep all valid events in memory: eight CSV fields plus source-derived category. Vehicle UUID is not an event key; retain duplicates (R-4.2–R-4.3). CSV dates (`DD/MM/YYYY`) become ISO API dates and `DD MMM YYYY` display dates; time remains `HH:mm:ss` (R-2.3).

**Planned R-6.4:** attach `audit: {sourceFile, sourceRow, ingestedAt}` at ingestion. Use the basename, one-based physical starting line (header = 1), and UTC ISO instant. Preserve metadata through every API path; display it in details with UTC labelled. Match cached selections by values including audit metadata, distinguishing identical source rows; render the API event. Metadata identifies an event within an ingestion, not a permanent database ID.

Independent `currentpath/<vehicle-uuid>` popups restore a matching selection or show a sole event/chooser. Close preserves list state; detail UUID lookups are uncapped.

`POST /api/vehicles/search` accepts optional `query`, `startDate`, `endDate`, `type`; returns `records`, `totalMatches`, `truncated`. AND trimmed case-insensitive UUID-prefix/brand-substring matching, exact type, and inclusive `start_date >= startDate` / `end_date <= endDate`; dates exclude time. Fields precede Search, then status filters. No initial results; blank Search requests all matches, edits wait for submission. Filter before paging; either/both statuses work. Search/status changes reset the page.

## Security & threat model

Treat CSV, queries and browser storage as untrusted. Validate schema, presence, types, UUIDs, lengths and status/source consistency; reject without rewriting, logging source/row/reason without raw values (R-6.1). Search validates JSON types, ISO dates and 100/50-character query/type bounds. Angular safe bindings and detached popup openers address R-6.2, including planned audit display.

**R-6.3 stance:** dev-only anonymous access. Planned guard: start without credentials only under explicit `dev`; default, `prod`, and mixed `prod,dev` must refuse startup without authentication. Production closure requires login, per-request admin authorisation and external credentials; identity integration remains deferred. No credentials are currently needed; future secrets use environment/mounted files (R-6.5). Planned R-6.6 errors use only `{code,message}` with generic messages and appropriate HTTP status, excluding diagnostic paths/traces/versions. Audit state is in memory; persistence must retain it alongside events. Compromised-server/tamper-proof audit protection is outside this exercise's local threat model.

## One explicit trade-off

The 2,000-event cap bounds browser work but can leave indistinguishable matches unreachable. R-6.4 provenance does not resolve this R-4.5 history-access limitation.

## Open questions

- Confirm the 100/50-character brand/type bounds and source-correction ownership.
- Future detail links: database record UUIDs, or vehicle UUID URLs with API-backed event tabs? Exact-event sharing still needs a stable event identifier.
- Confirm calendar/history-access departures, define the performance benchmark, and choose production identity/admin roles.
