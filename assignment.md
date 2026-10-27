# Take-Home Assignment

## 1. Purpose

This exercise tests four things, in line with our hiring bar for engineers joining the VRLS2 modernization track:

1. **Design thinking** — can you reason about ambiguous requirements, articulate trade-offs, and defend decisions in writing?
2. **Problem solving** — can you find defects, resolve conflicts in a spec, and ship a working vertical slice?
3. **Security consideration for coding** — do you recognise and mitigate common risks (OWASP top-10 in shape, if not by name) as you write code, not after?
4. **Applying AI in coding** — can you use AI tooling well: prompting with context, reading output critically, and overriding it when needed?

The assignment is a scoped slice of admin-side functionality. The code you ship matters; the way you reason about it matters more.

## 2. Process

| Step | Duration | Format |
|---|---|---|
| 1. Preliminary assignment | **3 calendar days** | Solo, async. We email you a single assignment zip (this brief + starter code). Create your own private GitHub repository, push the unmodified zip contents as your baseline commit on `main`, then follow the workflow in §7. A free personal GitHub account is sufficient. |
| 2. Submission | End of day 3, 23:59 SGT | Reply to the assignment email with: a link to your private GitHub repo, a read-only collaborator invite for the named panel members, and a ≤5-min demo recording link. |
| 3. Face-to-face meeting | **2 hours, on-site** | You demonstrate your AI generation process and your design thinking for the assignment. Agenda in §13. |

## 3. Stack

- **Frontend:** Angular with TypeScript. **Mandatory.**
- **Backend:** Java with Spring Boot. **Mandatory.**
- **Provided starter:** Build on the Angular frontend and Spring Boot backend already present in the starter repository. You may add libraries, refactor freely, and replace individual modules. You may **not** discard the starter and submit a freshly-scaffolded application — re-scaffolding bypasses the seeded defects and is out of scope for this exercise.
- **Datastore / persistence:** If you introduce persistence beyond the supplied CSV ingestion flow, the choice is yours (H2, embedded Postgres, etc.). Justify it in the ADR.
- **AI tooling:** Encouraged. We will ask what helped and where you overrode the AI, and we will watch you use it live in the face-to-face.

## 4. Assignment Package

You will receive this brief and the starter codebase together in a single **zip file** attached to the assignment email. Unpack it, create your own private GitHub repository, and push the unmodified contents (both `assignment.md` and the `starter/` tree) as the **baseline commit** on `main`. This baseline commit is the only direct commit to `main` you are permitted to make — everything after it must follow the workflow in §7.

The zip contains:

- `assignment.md` — this brief
- `starter/README.md` — how to run the starter
- `starter/docker-compose.yml` — brings up the whole stack with `docker compose up --build` from inside `starter/`
- `starter/data/` — four CSV files: `personal.csv`, `commercial.csv`, `deregpersonal.csv`, `deregcommercial.csv`
- `starter/apps/web/` — Angular frontend skeleton (one page, list view, minimal styling)
- `starter/apps/api/` — Spring Boot backend exposing `GET /api/vehicles`
- `starter/apps/api/Dockerfile`, `starter/apps/web/Dockerfile` — container images used by `docker-compose.yml`
- `starter/.github/ISSUE_TEMPLATE/` — issue templates ready to use; they activate automatically when you push to GitHub

The starter compiles and runs, but it is **deliberately incomplete and contains defects**. Discovering them is part of the assignment.

The intent is to debug, complete, and improve this codebase. Re-scaffolding from scratch is out of scope (see §3) and will not be assessed.

## 5. Requirements

All requirements are numbered. Reference them as `R-x.y` in your issues, ADR, and commit messages.

### 5.1 Functional Requirements

- **R-1.1** Admins must view all vehicles in two view modes: a **calendar view** (monthly grid) and a **list view** (paginated table).
- **R-1.2** Both views must clearly distinguish vehicles by status (`REGISTERED`, `DEREGISTERED`).
- **R-1.3** The list view must paginate at **50 records per page**.
- **R-1.4** The list view must default to sorting by **registration date, descending (newest first)**.
- **R-1.5** Admins must be able to filter vehicles by **status**, **vehicle category** (personal / commercial), and **vehicle type** (e.g. `5-seat`).
- **R-1.6** Clicking a row or calendar entry must open a **detail view** showing all fields for that vehicle.
- **R-1.7** The calendar view must show every vehicle on its corresponding day, and each vehicle must appear **exactly once per day cell**.
- **R-1.8** A search box must allow free-text search by vehicle UUID prefix or brand.

### 5.2 Display Standards

- **R-2.1** All tabular data displayed to the admin must be sorted **alphabetically by vehicle UUID** by default, to give a stable visual order across sessions.
- **R-2.2** Status badges must be colour-blind safe (do not rely on red/green alone).
- **R-2.3** Dates must be rendered as `DD MMM YYYY` (e.g. `08 Apr 2026`).
- **R-2.4** The UI must be usable on a 1280×720 desktop browser. Mobile responsiveness is a bonus.

### 5.3 Performance & Scale

- **R-3.1** All filtering, sorting, and search must be performed **on the client** so that admins get instant feedback with no network round-trip.
- **R-3.2** The system must handle the full historical dataset, expected to grow to **~250,000 vehicle records by 2027**.
- **R-3.3** Time-to-interactive on the list view must be under **2 seconds** on a standard office network.

### 5.4 Data Handling

- **R-4.1** The four CSVs are the source of truth. Your application must ingest them on startup or on demand.
- **R-4.2** Each CSV row represents a status event for a vehicle. The `uuid` column is the vehicle identifier.
- **R-4.3** A vehicle may appear in more than one CSV (e.g. registered, then later deregistered) and may have multiple events in either file.
- **R-4.4** The calendar view must show each vehicle on the day of its **most recent status change**, reflecting its current status.
- **R-4.5** Every status event must remain visible to the admin so that the registration history is fully **auditable**.

### 5.5 Non-functional

- **R-5.1** Code must live in your private GitHub repository with meaningful, atomic commits.
- **R-5.2** Test coverage must be sufficient to support the TDD constraint in §7. Aim for **≥70% line coverage** on changed code.
- **R-5.3** The application must remain usable when ingested CSV rows contain malformed, missing, or unexpected fields.

### 5.6 Security

Security is a first-class concern for this exercise, not a follow-up. We expect to see these addressed in code, in the ADR, or — where a deliberate deferral is the right call — declared explicitly in the ADR's Open Questions.

- **R-6.1** All ingested CSV rows must be validated before any field reaches persistence or the client. At minimum: field presence, type, length bounds, and value allow-lists where applicable (e.g. `status` ∈ {`REGISTERED`, `DEREGISTERED`}). Malformed rows must be rejected or quarantined with a structured log entry — silent "sanitisation" that rewrites input is **not** acceptable. (Intersects with R-5.3.)
- **R-6.2** All user-supplied fields (`uuid`, `brand`, `type`, `user_uuid`) must be rendered using Angular's safe-binding primitives (interpolation, property binding). Do not use `innerHTML` on untrusted data; `DomSanitizer.bypassSecurityTrust*` is acceptable only with a documented justification.
- **R-6.3** The admin API must not be anonymously accessible in production. Declare your authentication and authorisation stance in the ADR — full JWT, session cookie, or a dev-only bypass **with a documented production closure plan**. "Stub", "TODO", or leaving the endpoint open with no plan is not a stance and is a failing answer.
- **R-6.4** The ingestion and serving paths must be auditable: any displayed record must be traceable to its source row and its ingestion time. In-memory auditability is acceptable for this exercise. If you introduce persistence, the audit trail must persist alongside the data. (Intersects with R-4.5.)
- **R-6.5** No secrets in the repository. Configuration (DB credentials, API keys, signing keys) must come from environment variables or mounted files. `docker-compose.yml` must not hardcode production-shaped credentials.
- **R-6.6** Server error responses must not leak stack traces, internal paths, or framework versions. Use a controlled error envelope.

## 6. Reading the Spec Carefully

Real specs are written by multiple people over time and contain rough edges. This one does too. Some of those rough edges are accidental, some are deliberate. Part of your job is to find them, raise them as issues, and decide how to resolve them — exactly as you would on a real project.

We have numbered every requirement so you can reference them precisely. If you find a contradiction, file an issue referencing all the requirement numbers involved.

The intended sequence is: read the brief, raise issues, write the ADR, then start the red → green → refactor cycle.

## 7. Issue-Driven Development (IDD)

**No code may be changed without an open GitHub Issue on your repo, and no code may be committed outside that issue's dedicated branch.** This is a hard gate.

The workflow for every change is fixed:

1. **File an issue** using the provided template (see fields below)
2. **Cut a fresh branch off `main`** named `issue-<number>-<short-slug>` (e.g. `issue-7-fix-date-parsing`). One branch per issue.
3. **Write a failing test first** on that branch (see §8)
4. **Make it pass**, refactor, push, open a Pull Request that references the issue with `Closes #<number>`
5. **Merge to `main`** with a **merge commit** (do **not** squash). Each PR's red → green → refactor commits must land on `main` intact.

**The only commit allowed directly on `main` is the initial baseline commit** containing the unmodified starter as you received it in the zip. Every subsequent change to `main` must arrive via a merged PR from an issue branch. The panel will inspect the PR list and `main`'s commit history, and will reject any direct commits to `main` after the baseline.

Every issue must use the provided template and contain:

1. **Problem statement** — what is wrong or missing, in user-visible terms
2. **Root cause hypothesis** — your best current theory (you may be wrong; that is fine, update the issue when you learn more)
3. **Acceptance criteria** — specific, testable conditions that mean the issue is resolved
4. **Requirement reference** — which `R-x.y` this maps to, or `discovered-defect` if it does not map to a stated requirement

Expect to file several issues across the assignment. For most candidates over three days, this lands around **4–6 issues**, but quality matters far more than count.

Issues must be in your **own words**. You may use AI as an editor or challenger, but do not submit lightly edited AI-generated issue text as if it were your analysis. (See §11 — log the assist.)

> **Hard gate:** At least one GitHub Issue must explicitly surface a requirement conflict in §5 before you implement code related to that area. Stronger submissions identify and resolve more than one genuine conflict.

## 8. Test-Driven Development (TDD)

For any issue that results in production code changes, the branch history must show:

1. Write a failing test that encodes the acceptance criteria
2. **Commit the failing test** — we will grep `git log main` for the red commit
3. Make it pass with the smallest viable change
4. Refactor

Because §7 prohibits squash merges, the red → green → refactor cycle lands on `main` intact. The panel inspects `main` only — there is no need to walk individual PR commit histories.

Documentation-only issues do not require tests, but no production code may be added before the relevant failing test exists.

## 9. Architecture Decision Record (ADR)

Before writing implementation code, produce a **single-page ADR** at `docs/adr/0001-vrls2-takehome.md` covering:

- **Context** — your understanding of the problem in 3–5 sentences
- **Conflicts found in §5** — for each material conflict you identified, name it and the decision rule you adopted
- **Data model** — entities, keys, and how you handle vehicles with multiple status events
- **Security & threat model** — which §5.6 controls apply where, what threats you leave out deliberately, and why. Your **authentication & authorisation stance (R-6.3)** must appear here.
- **One explicit trade-off** — something you deliberately gave up, and why
- **Open questions** — what you would ask the product owner if you could

Length: **one page**. Clarity beats completeness.

## 10. Architecture Diagram

Produce **one architecture diagram** (Python, Mermaid, draw.io, or even a clean hand-drawn photo) showing:

- Frontend, backend, and data layers
- How CSV ingestion fits in
- Where the requirement conflicts you addressed are resolved (e.g. server-side aggregation vs client-side filtering)
- Where the §5.6 security controls live (validation boundary, auth enforcement, secret source, audit log)

Place it at `docs/architecture.{md,png,svg}`.

The diagram must describe your actual solution rather than a generic template.

## 11. AI Usage Log

Maintain `docs/ai-usage.md` throughout the build. For each issue or task where AI assisted you, record:

1. A one-line description of the task
2. The prompt(s) you used — abridged is fine, but preserve context, constraints, and non-goals
3. What the AI produced, and what you kept, refined, or rejected — and why
4. Where AI accelerated you vs where you had to override it

This is a **scored deliverable**. In the face-to-face (§13) we will compare what you wrote here to what you do live. Heavy AI use is fine and expected. Hidden or undocumented AI use is not.

## 12. Deliverables Summary

| # | Deliverable | Location |
|---|---|---|
| 1 | GitHub Issues (typically 4–6, in your own words) | Issues tab of your private GitHub repo |
| 2 | ADR (one page) | `docs/adr/0001-vrls2-takehome.md` |
| 3 | Architecture diagram | `docs/architecture.*` |
| 4 | AI usage log | `docs/ai-usage.md` |
| 5 | Working application | `starter/apps/web/`, `starter/apps/api/` |
| 6 | Tests with red-then-green commits | `starter/apps/*/test/` or equivalent |
| 7 | README with run instructions | `README.md` |
| 8 | ≤5-minute demo recording | Link in README |
| 9 | Read-only invite to the panel | Sent in your submission email |

## 13. Face-to-Face Meeting Agenda (2 hours, on-site)

| Block | Duration | What |
|---|---|---|
| Submission walkthrough | 30 min | You walk us through the repo, ADR, architecture diagram, and AI usage log. We ask sharp, specific questions. |
| Live AI exercise | 45 min | We present a small scoped task on your codebase. You complete it using the AI tooling of your choice (Claude Code, Cursor, Copilot, etc.), narrating your prompts, your rejections, and your overrides. We score how you apply AI — not how fast you ship. |
| Design defense + relaxed-constraint rework | 30 min | Defend one §5 conflict resolution of our choosing. Then rework one decision under a constraint we relax on the spot. |
| Q&A and logistics | 15 min | Open questions, next steps. |

## 14. Evaluation

Weighted against the four purposes of this test (§1):

- **Design thinking — 30%**: ADR coherence, conflict identification and resolution, architecture diagram quality, decision ownership in the face-to-face defense.
- **Problem solving — 30%**: issue quality, defect discovery, test discipline (IDD + TDD), a working vertical slice, minimum scope.
- **Security consideration — 15%**: §5.6 requirements met in code; ADR threat-model and R-6.3 auth stance; no obvious OWASP top-10 gaps introduced in your diff.
- **Applying AI in coding — 20%**: AI usage log authenticity; live AI exercise in the face-to-face; clear override moments.
- **Demo & defense — 5%**: Can you walk us through what you built and revise a decision when a constraint changes?

Submitting more code with no issues is **worse** than submitting fewer features with clear issues.

## 15. Things We Will Not Penalise

- Unfinished bonus work
- Staying close to the supplied starter architecture
- Disagreeing with us in the ADR — we like that, if it is reasoned
- Heavy AI use, as long as it is logged (§11) and you can explain every line you submitted

## 16. Things That Will Disqualify

- Code changes with no corresponding Issue
- Any commit to `main` other than the initial baseline starter commit and merged PRs
- Production code added without a failing test first
- No GitHub Issue surfacing a requirement conflict in §5
- An ADR that does not address the material conflicts you raised
- An ADR missing a security / threat-model section or missing the R-6.3 authentication stance
- No AI usage log, or a log that is obviously fabricated
- AI-generated issues passed off as your own analysis. We confirm in the face-to-face: you must be able to reconstruct and defend the Issue in your own words. Verbatim or lightly-edited AI output presented as your analysis is a fail.

## 17. Files

The starter repo contains four CSV files, identical in schema to the ones used in the v1 assignment:

1. `personal.csv` — registered personal vehicles
2. `deregpersonal.csv` — de-registered personal vehicles
3. `commercial.csv` — registered commercial vehicles
4. `deregcommercial.csv` — de-registered commercial vehicles

Schema: `uuid, status, start_date, end_date, time, user_uuid, brand, type`. Dates are in `DD/MM/YYYY` format. Times are in 24-hour `HH:mm:ss`. No timezone is recorded — assume Singapore time (UTC+8).
