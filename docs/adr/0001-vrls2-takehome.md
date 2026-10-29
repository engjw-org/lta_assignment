# ADR 0001 — VRLS2 take-home

## Context

VRLS2 retains the supplied Angular/Spring Boot starter and four source CSVs. [Issue #1](https://github.com/engjw/lta_assignment/issues/1) adds validated ingestion/date display; [#3](https://github.com/engjw/lta_assignment/issues/3) adds safe, responsive detail popups. [Issue #5](https://github.com/engjw/lta_assignment/issues/5) adds submitted server searches capped at 2,000 events with local status filters/paging. [Issue #7](https://github.com/engjw/lta_assignment/issues/7) adds in-memory provenance, a dev-only startup guard and controlled API errors.

## Conflicts found in §5

- **R-1.4 / R-2.1:** full UUID ascending takes precedence; registration (`start_date`) descending breaks ties within each UUID.
- **R-1.1 / R-1.7 / R-4.4 versus R-3.2 usability:** use list date selection instead of a calendar. This is a usability choice, not a logical contradiction; calendar requirements remain unmet.
- **R-1.8 / R-3.1 / R-3.2 / R-3.3:** search all valid events server-side, sort, then cap at 2,000. This departs from R-3.1; status filtering and 50-row paging remain local (R-1.3). Show total matches and a refine-search notice when truncated. No continuation; history access is limited (R-4.5). The two-second target needs measurement.

## Data model

Keep all valid events in memory: eight CSV fields plus source-derived category. Vehicle UUID is not an event key; retain duplicates (R-4.2–R-4.3). CSV dates (`DD/MM/YYYY`) become ISO API dates and `DD MMM YYYY` display dates; time remains `HH:mm:ss` (R-2.3).

**R-6.4:** attach immutable `audit: {sourceFile, sourceRow, ingestedAt}` after row validation. Use the basename, one-based physical starting line (header = 1), and UTC ISO ingestion instant. Preserve metadata through every API path; details display `DD MMM YYYY HH:mm:ss UTC`. Match cached selections by values including audit metadata, distinguishing identical source rows; render the API event and restore the chooser selection. Metadata identifies an event within an ingestion, not a permanent database ID; re-ingestion invalidates old selection hints.

Independent `currentpath/<vehicle-uuid>` popups restore a matching selection or show a sole event/chooser. Close preserves list state; detail UUID lookups are uncapped.

`POST /api/vehicles/search` accepts optional `query`, `startDate`, `endDate`, `type`; returns `records`, `totalMatches`, `truncated`. AND trimmed case-insensitive UUID-prefix/brand-substring matching, exact type, and inclusive `start_date >= startDate` / `end_date <= endDate`; dates exclude time. Fields precede Search, then status filters. No initial results; blank Search requests all matches, edits wait for submission. Filter before paging; either/both statuses work. Search/status changes reset the page.

## Security & threat model

Treat CSV, queries and browser storage as untrusted. Validate schema, presence, types, UUIDs, lengths and status/source consistency; reject without rewriting, logging source/row/reason without raw values (R-6.1). Search validates JSON types, ISO dates and 100/50-character query/type bounds. Angular safe bindings include audit display; popup openers are detached (R-6.2).

**R-6.3 stance:** anonymous startup requires `dev` as the only active profile; default and all other combinations fail before startup completes. Compose selects `dev` and binds to localhost. Production remains disabled until login, per-request admin authorisation and external credential configuration are implemented; setting credentials alone cannot unlock it. Future secrets use environment/mounted files (R-6.5). Controller exceptions and servlet errors return only `{code,message}` with generic messages and appropriate HTTP status; internal diagnostics stay in server logs (R-6.6). Audit state is in memory; persistence must retain it alongside events. Compromised-server/tamper-proof audit protection is outside this exercise's local threat model.

## One explicit trade-off

The 2,000-event cap bounds browser work but can leave indistinguishable matches unreachable. R-6.4 provenance does not resolve this R-4.5 history-access limitation.

## Open questions

- Confirm the 100/50-character brand/type bounds and source-correction ownership.
- Future detail links: database record UUIDs, or vehicle UUID URLs with API-backed event tabs? Exact-event sharing still needs a stable event identifier.
- Confirm calendar/history-access departures, define the performance benchmark, and choose production identity/admin roles.
