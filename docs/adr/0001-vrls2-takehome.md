## Context

The detail component displays only `TODO`, so clicking a list row cannot show all vehicle fields (R-1.6). The user wants separate browser popups at `currentpath/uuid` for comparison while retaining the original list state. Details must support later input, readable dates, a responsive grid and safe rendering (R-2.3, R-2.4, R-6.2).

## Decision

- Open each different vehicle in an independent popup at `currentpath/<full-uuid>`; from the root list this is `/<uuid>`. Keep the list's page, filters and row order. Provide a labelled Close control and visible feedback if the browser blocks opening.
- Reuse the detail component's complete `vehicle` input and `close` output. Support delayed input and updates without mutating the record; keep simultaneous instances independent. Show the clicked event's values when a UUID has multiple events.
- Display labelled values for all CSV fields and category, including full UUIDs. Format both dates as `DD MMM YYYY`; preserve `HH:mm:ss` time. Use multiple grid columns at 1280×720 and one at 360×640, wrapping long values without horizontal overflow.

## Data model and requirement ambiguity

The existing `Vehicle` model already has `uuid`, `status`, `startDate`, `endDate`, `time`, `userUuid`, `brand`, `type` and `category`; no missing field requires a model change. Each row remains an event; UUID identifies the vehicle, not a unique event (R-4.2, R-4.3). R-1.6 therefore requires preserving the selected row during popup handoff, rather than silently replacing it with the first UUID match. A bare URL for repeated UUIDs remains an open question, not a claimed requirement contradiction.

Existing issue #1 policy remains: strict CSV date/time validation, rejection diagnostics and unchanged source files (R-4.1, R-6.1). Brand/type bounds remain the proposed 100/50 characters pending confirmation.

## Security and threat model

Render untrusted fields through Angular interpolation/property binding; do not use `innerHTML` or bypass sanitisation. Tests require markup in `uuid`, `brand`, `type` and `userUuid` to remain literal text without injected elements. Backend validation remains the ingestion boundary; popup data is not a new trust boundary exemption.

Retain the R-6.3 stance: anonymous access only in explicitly enabled local development; before production, require login, check admin permission on every API request and block startup if authentication configuration is missing. Those authentication controls are planned, not implemented here. Compromised-server protection is outside scope.

## Trade-off

Separate windows permit comparison and preserve the list, but browser popup policy can prevent opening. Explicit feedback makes that failure visible.

## Test evidence and open question

Added 21 frontend tests: **20 fail, 1 passes**; both earlier date-display tests still pass. All **103 backend regression tests pass**. Tests cover fields, dates, input updates, safe text, closing, popup requests, routing and real component layouts in sized iframes. They do not yet prove cross-window data transfer, browser close/return behaviour or full application layout. Measure the R-5.2 ≥70% changed-code coverage target during implementation.

For a repeated UUID opened directly or refreshed without the clicked-row payload, which event should be shown? Resolve this before implementing that lookup; do not silently choose the first event.
