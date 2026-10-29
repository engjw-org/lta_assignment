# ADR 0001 — VRLS2 take-home

## Context

VRLS2 extends the supplied Angular/Spring Boot starter, using four CSVs as the source of truth. [Issue #1](https://github.com/engjw/lta_assignment/issues/1) fixes date/time parsing, CSV validation and date display (R-4.1, R-6.1, R-2.3). [Issue #3](https://github.com/engjw/lta_assignment/issues/3) adds safe, responsive popups showing every vehicle field while preserving list state (R-1.6, R-2.4, R-6.2). This shared ADR records decisions across both issues; future proposals remain explicit.

## Conflicts found in §5

CSV dates (`DD/MM/YYYY`) and displayed dates (`DD MMM YYYY`) serve different purposes and are compatible: validate the source format, then format for display.

R-1.4 requires newest-registration-first sorting; R-2.1 requires UUID-alphabetical sorting by default. No precedence rule is approved. Issues #1/#3 do not raise this conflict; a dedicated issue and decision are still required before related implementation (§7).

## Data model

Keep validated events in memory; no database is introduced. Each record contains the eight CSV fields plus category derived from its source file. Vehicle UUID identifies a vehicle, not an individual event; retain every valid repeated-UUID event (R-4.2, R-4.3, R-4.5). Parse dates/times strictly, expose ISO dates through the API, and render dates as `DD MMM YYYY` with time as `HH:mm:ss`.

Open `currentpath/<vehicle-uuid>` in an independent popup. Match the clicked record's per-window session snapshot against all validated API events for that UUID. Refresh restores a matching selection; otherwise show a sole event or a chooser. Close dismisses the popup without changing the original list. The layout supports 1280×720 and a single column on narrow screens.

## Security & threat model

Treat CSV input and browser storage as untrusted. Validate schema, required fields, types, UUIDs, lengths and status/source consistency before serving records (R-6.1). Reject malformed rows without rewriting source data; log file, row, field and reason without raw values. Render through Angular safe bindings, never `innerHTML` or sanitiser bypasses; detach popup openers (R-6.2).

**R-6.3 stance:** the API currently permits anonymous access for local development. Before production, require login, enforce admin permission per request and refuse startup without authentication configuration; these controls are not implemented. Keep secrets in environment configuration (R-6.5). Accepted-record provenance/ingestion times (R-6.4) and uniform API error envelopes (R-6.6) remain outstanding. Compromised-server protection is outside this local assignment's input-validation threat model.

## One explicit trade-off

Strict rejection keeps malformed data out of the application, but rejected events remain unavailable until corrected at their source. Preserve valid neighbouring records and structured rejection logs so the application stays usable (R-5.3).

## Open questions

- Confirm the implemented brand/type limits of 100/50 characters and ownership of rejected-row corrections.
- For durable event links, prefer a separate record UUID with a proper database, or retain vehicle UUID URLs with API-backed event tabs defaulting to the clicked record? Tabs would replace the current chooser; sharing an exact event still needs a stable event identifier. These are future options.
- Which production identity provider and admin-role model should implement R-6.3?
