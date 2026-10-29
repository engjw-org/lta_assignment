# AI usage log

Maintained according to `assignment.md` §11. The user completes fields 1 and 4 and adds their assessment to field 3. The agent records each prompt in field 2 and its answer and actual output in field 3.

## Entry 001 — 27 Oct 2026

### 1. Task description

_User to complete._
Init the agent file

### 2. User prompt(s)

```text
create a agent file

1. anything not sure can refer to assignment.md
2. every prompt need to record down based on 11. AI usage log, 1 and 4 I will fill up, you focus on 2 and 3 which my prompt in 2. and your answer in 3. (i will update my part on 3 also)
3. 16\. Things That Will Disqualify, remind me if my prompt or the code changes might trigger the things listed
```

### 3. AI output and user assessment

**AI answer:**

Created `AGENTS.md` with instructions to consult `assignment.md`, log every prompt and answer, and flag all eight §16 disqualification risks.

Started `docs/ai-usage.md` with this request. Fields 1 and 4 and your assessment in field 3 are left for you.

Reminder: you're on `main`; commit these files through an issue branch and PR, not directly to `main`.

**Work produced:**

- Read the assignment, including §§7–9, 11, and 16, and checked the current branch (`main`).
- Added the root agent instructions and this initial log entry. No production code was changed and no commits were made.
- Documentation-only work does not require tests under §8.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._
I reviewed the agents.md file and the ai-usage.md file, it worked as expected so I kept it.

### 4. Where AI accelerated you vs where you overrode it

_User to complete._
Assisted me to document down every prompt

## Entry 002 — 27 Oct 2026

### 1. Task description

Check on data format in csv, after read point 17

### 2. User prompt(s)

```text
help me check does all csv files data with correct data format as mentioned
```

### 3. AI output and user assessment

**AI answer:**

Not all rows match the specified format. Checked all 234 data rows in the four CSVs against `assignment.md` §17. Eight rows have times without the leading zero required by `HH:mm:ss`; 226 rows passed the performed format checks.

Line numbers below include the header as line 1.

| File | Data rows | Time format findings |
| --- | ---: | --- |
| `starter/data/personal.csv` | 42 | None |
| `starter/data/commercial.csv` | 34 | Line 33: `9:30:00` (specified format: `09:30:00`) |
| `starter/data/deregpersonal.csv` | 98 | Lines 9 and 21: `9:00:00`; line 11: `9:30:00` (specified format: `09:00:00` / `09:30:00`) |
| `starter/data/deregcommercial.csv` | 60 | Lines 2, 3, 8, and 41: `9:00:00` (specified format: `09:00:00`) |

All four headers match `uuid,status,start_date,end_date,time,user_uuid,brand,type`. Every row has exactly eight populated fields. Both identifier columns match canonical UUID-shaped text. Status values are allowed and match the file descriptions. All 468 date values are valid calendar dates in exactly `DD/MM/YYYY` format. No surrounding whitespace, control characters, or end dates before start dates were found. No repeated vehicle identifiers or exact duplicate rows were found across the current sample; repeated vehicle identifiers would not by themselves be errors because R-4.3 permits multiple events.

The assignment does not give exact text length limits or complete brand/type allow-lists, so those policies cannot be certified from the brief alone. UUID shape checks do not establish real-world identity or ownership.

A separate code defect was found by inspection: `starter/apps/api/src/main/java/sg/lta/vrls2/service/VehicleCsvLoader.java:22` uses `MM/dd/yyyy`, while the source format is day/month/year. It can interpret valid CSV dates incorrectly or return `null` when parsing fails (lines 57–61). The time parser also returns `null` on parsing failure (lines 65–69). This is a static code finding; the application was not run for this review.

R-6.1 requires rejecting or quarantining malformed input with a structured log rather than silently rewriting it. Under §16, a production code fix needs an open issue, its dedicated branch, and a committed failing test first. Do not commit directly to `main` after the baseline.

**Work produced and validation:**

- Used the Spreadsheets skill and a read-only Python standard-library CSV scan over every row. Checked CSV parsing, exact schema and field counts, nonblank fields, UUID shapes, status values, strict date/time shapes and calendar validity, whitespace/control characters, date ordering, lengths, and duplicates.
- Confirmed all eight reported source lines directly and inspected the backend parser. No CSV or application code was changed; `git diff --exit-code -- starter/data starter/apps` showed no changes. No commits were made.
- Appended this entry while preserving the user's additions to entry 001. Fields 1 and 4 and the user assessment below remain for the user.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._
keep all after review the csv and code
so based on the result, only time format is wrong in csv, and it also found out the date format parse wrongly in code
next, i will based on the found in this prompt and create the github issue

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 003 — 28 Oct 2026 — Issue #1

### 1. Task description

_User to complete._
prepare unit test and create ADR page

### 2. User prompt(s)

Formatting normalised; wording retained:

```text
1. Problem statement — what is wrong or missing, in user-visible terms
- wrong display format for start date data
- csv file with wrong time format in column of "time"

2. Root cause hypothesis — your best current theory (you may be wrong; that is fine, update the issue when you learn more)
- csv file is with wrong format
- wrong parse in backend

3. Acceptance criteria — specific, testable conditions that mean the issue is resolved
- able to show start date data with correct format
- backend able to validate the data for csv file, based on R-6.1
- pass unit test

4. Requirement reference — which R-x.y this maps to, or discovered-defect if it does not map to a stated requirement
R-2.3 Dates must be rendered as DD MMM YYYY (e.g. 08 Apr 2026).
R-4.1 The four CSVs are the source of truth. Your application must ingest them on startup or on demand.
R-6.1 All ingested CSV rows must be validated before any field reaches persistence or the client. At minimum: field presence, type, length bounds, and value allow-lists where applicable (e.g. status ∈ {REGISTERED, DEREGISTERED}). Malformed rows must be rejected or quarantined with a structured log entry — silent “sanitisation” that rewrites input is not acceptable. (Intersects with R-5.3.)

here my github issue, i need your help on
1. i have created the feature based on the issue 1
2. i need the unit test like mentioned in point 8, that able to not only handle the parse issue in backend but also check all csv data with R6.1 rules
3. it no need any code changes, need the failing test first
4. after done 1 to 3, prepare the ADR page for me with the correct format with this issue
```

### 3. AI output and user assessment

**AI answer:**

Added and ran the failing tests on the existing `issue-1-fix-date-parsing` branch, then created local red-test commit `3d99d09`. No production code or CSV data was changed. Prepared the one-page ADR at `docs/adr/0001-vrls2-takehome.md`, covering context, conflicts, data model, security/threat model with R-6.3 authentication stance, trade-off, and open questions. The ADR marks implementation and unconfirmed decisions as proposed.

Validation results:

| Suite | Executed | Passed | Assertion failures | Test errors | Skipped |
| --- | ---: | ---: | ---: | ---: | ---: |
| Backend (includes the original controller test) | 103 | 10 | 93 | 0 | 0 |
| Angular start-date display | 2 | 0 | 2 | 0 | 0 |

These failures demonstrate the required red phase. They do not indicate the feature is implemented or issue #1 is resolved. Keep the red commit intact when eventually merging with a merge commit rather than squashing (§§7–8).

The assignment does not specify exact text bounds. The tests and ADR propose brand length 1–100 and type length 1–50, with no surrounding whitespace or control characters. Brand/type values are not restricted to the sample data. Other explicit proposed contracts are exact schema/order, required fields, canonical UUID shape, status/file consistency, strict date/time formats, and structured rejection fields `source`, `row`, `field`, `reason` (JSON or SLF4J key-values). Review these choices before implementing them; they are not all literal requirements from the brief.

**Disqualification reminder:** Issue #1 records defects but does not explicitly surface a conflict between §5 requirements. The ADR identifies proposed resolutions for sorting, client-side scale/performance, and current-state/history requirements. The user must raise and defend a conflict issue in their own words before related implementation; this draft ADR does not satisfy the GitHub issue gate by itself (§§7, 16).

**Work produced and validation:**

- Verified issue #1, “Date Time format issue”, is open in `engjw/lta_assignment` through the authenticated browser after the GitHub connector returned access errors. Read only; no issue text or comments were posted or changed. Used the Computer Use skill for this verification.
- Confirmed the user's existing issue branch initially pointed to baseline `eb0760d`, with no production changes. There was no earlier implementation commit to misrepresent as test-first work.
- Added `VehicleCsvLoaderValidationTest.java` (98 executions) for both date columns, strict times, missing/blank fields, UUID shape, status allow-list and file consistency, text bounds, whitespace/control characters, missing/extra columns, header changes, rejection diagnostics, continuation after invalid records, category preservation, quoted/unicode values, and repeated-UUID events.
- Added `VehicleCsvLoaderDatasetTest.java` (four executions) exercising the actual loader against all 234 source rows. Expected accepted counts are 42 personal, 33 commercial, 95 deregistered personal, and 56 deregistered commercial. Every accepted record's fields are compared with source-derived expectations. The eight known malformed time rows must be excluded and individually logged. The tests do not make the CSVs pass by rewriting them or replace the real loader with a test validator.
- Added `CsvTestSupport.java` for temporary CSV fixtures and `CsvRejectionLogs.java` to observe actual application logging. Fixture tests check valid neighbouring records survive and source files remain unchanged.
- Added `vehicle-list.component.spec.ts` with two component tests: `2026-04-08` must render as `08 Apr 2026`, and `2022-11-01` as `01 Nov 2022`. Both fail on the current raw-date output. Backend parsing tests alone would not verify R-2.3.
- Ran the existing backend suite before adding tests: one test passed. Ran the expanded backend suite in the official `maven:3.9-eclipse-temurin-21` container with Java 21; tests compiled and produced the assertion failures above, with no test errors. Ran `npm test -- --progress=false` in headless Chrome for the frontend. Installed declared frontend dependencies without creating a lockfile or changing package files.
- Backend reproduction: from `starter/apps/api`, run `mvn test` with Java 21 and Maven 3.9+. Frontend reproduction: from `starter/apps/web`, run `npm test -- --progress=false` with Chrome available. Current failures are expected until the green phase.
- Wrote a 483-word ADR, marking broader architecture/security measures as planned rather than falsely claiming they exist. Preserved the user's additions to earlier log entries and left fields 1 and 4 and the assessment below for the user.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 004 — 28 Oct 2026 — Manual commit preference

### 1. Task description

_User to complete._
revert commit, and make sure AI will not auto commit again

### 2. User prompt(s)

```text
revert back 2 commits, can put in agent file, i will manually commit after review no issue
```

### 3. AI output and user assessment

**AI answer:**

Undid the two local commits (`3d99d09` and `3cd4e8f`), returning `issue-1-fix-date-parsing` to baseline `eb0760d` while preserving all tests, the ADR, and the AI usage log as uncommitted files. Updated `AGENTS.md`: the user reviews and commits manually; the agent must not stage, commit, rewrite history, push, or merge without an explicit request for that action. Following §8 or preparing tests/documentation is not permission to commit.

The failing tests still need to be committed manually after review and before related production changes (§§8, 16). Entry 003 records the earlier actions historically; its commits have now been undone at the user's request.

**Work produced and validation:**

- Verified the issue branch and exact two-commit sequence before undoing them with a mixed reset, preserving working files and leaving the index empty.
- Compared SHA-256 hashes of all seven files from those commits before and after the reset; every file was preserved unchanged. Then updated only `AGENTS.md` and appended this log entry.
- No production code or CSV data was changed. Tests were not rerun because their contents did not change. No new commits were created and nothing was pushed.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 005 — 28 Oct 2026 — Review issue #1 tests

### 1. Task description

_User to complete._
check unit test created

### 2. User prompt(s)

```text
explain the unit tests you added for issue 1 and why they fail, so i can review before manually committing
```

### 3. AI output and user assessment

**AI answer:**

The tests describe the intended behaviour before production fixes. They use the real CSV loader; failures are assertions about its output or resilience, not deliberate unconditional failures. The last saved run had 103 backend executions (10 passed, 93 failed, zero errors/skips) and two frontend executions (both failed). Many executions reuse a parameterised test with different inputs, so 93 failures do not mean 93 independent defects. These are saved results, not a new test run.

`VehicleCsvLoaderValidationTest.java` has 98 executions:

| Group | Cases | Failures | Explanation |
| --- | ---: | ---: | --- |
| Both date columns use day/month/year | 6 | 5 | The loader uses `MM/dd/yyyy`: `01/11/2022` becomes 11 Jan rather than 1 Nov, while a day greater than 12 produces a parse failure and `null`. The `01/01/2026` control passes because both orders give the same date. |
| Invalid field rejection, logging and continuation | 73 | 73 | Missing/blank fields, malformed UUIDs, disallowed/mismatched statuses, invalid date/time values, excessive lengths and whitespace/control characters are not rejected. The loader returns invalid records, possibly with null dates/times, and produces no structured rejection log. |
| Wrong field count | 3 | 3 | Short/blank rows can throw while indexing columns; extra fields are ignored instead of rejecting the row. Tests require valid neighbouring rows to remain available. |
| Invalid headers | 4 | 4 | The loader skips the first row without validating its names, order or width. Tests require rejection of that file and continuation with another valid file. |
| Validation applies to all four file types | 4 | 4 | Each file contains one valid and one malformed-time row. Both are returned instead of only the valid record, and the expected rejection log is absent. |
| Valid inputs and history preservation | 8 | 0 | Four valid clock times, two text-length boundaries, quoted commas/unicode/unseen brand/type values, and repeated UUID events already work. These protect working behaviour during the fix. |

The main invalid-field test creates three temporary rows: valid A, invalid B, valid C. It checks that ingestion does not throw, returns only A and C, records the invalid source/line/field/reason, and leaves the source file unchanged. `@TempDir` isolates fixtures from the supplied data. Its dates use `11/11/2022` to keep the known day/month-order bug from obscuring the validation checks. `assertDoesNotThrow` reports an unexpected application exception as an assertion failure; zero reported test errors does not mean the starter never throws on bad rows.

`VehicleCsvLoaderDatasetTest.java` adds four executions against all 234 supplied rows. It compares all fields of the expected accepted events and requires diagnostics for the eight known malformed-time rows. Expected accepted counts: personal 42, commercial 33, deregistered personal 95, deregistered commercial 56 (226 total). All four executions fail because accepted dates are wrong/null; three files also retain malformed-time rows without rejection logs. This is a regression check of the current sample, with known bad line numbers and counts, not a general-purpose validator for any future CSV. New malformed-input behaviour is exercised by the separate fixture cases.

`vehicle-list.component.spec.ts` adds two Angular component tests using a fake service response and the real template. They require `2026-04-08` to render as `08 Apr 2026` and `2022-11-01` as `01 Nov 2022`. The template currently renders the raw ISO string, so both fail. They check the rendered Start Date cell and that formatting does not mutate the source date. These are component tests, not an end-to-end browser/API test.

`CsvTestSupport.java` creates fixture CSVs; `CsvRejectionLogs.java` captures actual Logback events and recognises either JSON messages or SLF4J key-values. Neither helper implements the missing production validator or invents application logs. The original controller smoke test was not added by the agent and is the remaining passing backend execution.

Before committing, review the choices beyond literal assignment wording: all eight fields required, canonical UUID shape, strict header order, status/file consistency, brand/type limits of 100/50 characters, rejection of surrounding whitespace/control characters, and the exact rejection fields `source`, `row`, `field`, `reason`. R-6.1 requires validation, bounds, applicable allow-lists, rejection/quarantine and structured logging, but leaves these details partly unspecified. The ADR records them as proposed. No brand/type allow-list is inferred from Toyota/Ford or 5-seat/7-seat samples. There is no 70% coverage measurement, complete security proof, or full application end-to-end coverage in these test results.

After review, manually commit the failing tests on `issue-1-fix-date-parsing` before adding related production fixes (§8). This explanation does not stage or commit anything.

**Work produced and validation:**

- Read the current test files, loader, template, ADR, assignment, agent instructions and saved Maven/Karma reports. Derived the counts above from individual test cases in the saved XML reports.
- Confirmed all five test/support files still match the versions used for the earlier verified run. Did not rerun tests or change their contents.
- Observed existing changes to `starter/apps/web/angular.json` and an untracked `package-lock.json`; left them untouched. No production files or Git state were changed. Only this log entry was appended, preserving prior user additions.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 006 — 28 Oct 2026 — Narrow issue #1 ADR

### 1. Task description

_User to complete._
update ADR

### 2. User prompt(s)

```text
update the ADR based on my github issue and the unit test done, dun add any additional no related on this issue fix info
```

### 3. AI output and user assessment

**AI answer:**

Updated `docs/adr/0001-vrls2-takehome.md` around the issue #1 description provided by the user and the existing tests: strict date/time parsing, CSV validation, rejection logging and continued ingestion, and Start Date display. Included the recorded red-test results and clearly marked the fix as not yet implemented.

Removed unrelated sorting rules, scale/performance decisions, future current-state/history architecture, new event identifiers, ingestion-time data-model additions, database decisions and unrelated open questions. Kept the existing `Vehicle` representation and preservation of repeated UUID events because the tests directly cover these.

Kept the proposed 100/50-character text limits and logging contract visible for review because the tests already enforce them. Explained that differing CSV and screen date formats are compatible requirements, not a requirement conflict claimed by issue #1.

Retained only a short authentication stance alongside the issue-specific threat model because assignment §9 explicitly requires R-6.3 and §16 lists its omission as disqualifying. It is marked as a proposed production closure plan, outside issue #1 implementation. No additional authentication implementation was added.

**Work produced and validation:**

- Read the latest local ADR, assignment requirements, relevant test definitions and saved results. Used the issue text already supplied and previously verified in this conversation; did not claim a fresh GitHub issue fetch.
- Confirmed saved backend results of 103 tests with 93 failures and no errors, and frontend results of two failing display tests. Reviewed the dataset expectations of 234 source rows and 226 accepted rows. No tests were rerun for this documentation edit.
- Changed only the ADR and appended this log entry. Preserved prior user log additions and other working changes. Nothing was staged, committed, pushed or merged.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 007 — 28 Oct 2026 — Implement issue #1 and run the tests

### 1. Task description

_User to complete._

### 2. User prompt(s)

```text
now update the code and fix all the issue 1 found, and run the unit test
```

### 3. AI output and user assessment

**AI answer:**

Implemented the issue #1 date parsing, CSV validation and Start Date display fixes. All 103 backend tests and both frontend tests pass. The loader accepts 226 of the 234 supplied rows and rejects the eight malformed-time rows with structured diagnostics. The original CSV files and committed tests are unchanged. Changes are ready for review and a manual commit on `issue-1-fix-date-parsing`.

**Work produced and decisions:**

- Read the assignment, agent instructions, current ADR, production files and committed tests. Verified that GitHub issue #1, “Date Time format issue”, is open through the authenticated browser. Verified that commit `b1693e7` contains the previously run failing tests and ADR, with no related production fix, before changing production code. The working tree was initially clean.
- Updated `starter/apps/api/src/main/java/sg/lta/vrls2/service/VehicleCsvLoader.java` to parse both dates strictly as `DD/MM/YYYY` and times as 24-hour `HH:mm:ss`. Invalid calendar dates, year zero, single-digit hours and out-of-range times are rejected instead of becoming null or being normalised.
- Validate the exact eight-column header and row width, all required values, canonical UUIDs, status and source-file consistency, length limits, surrounding whitespace and control characters before constructing a `Vehicle`. Use the 100-character brand and 50-character type limits already encoded in the committed tests and ADR; these remain application policy pending product-owner confirmation, not exact limits supplied by the assignment. Do not infer brand/type allow-lists from the sample data.
- Read records individually, retain valid neighbouring records and repeated UUID events, and log each rejected record as JSON with `source`, physical starting `row`, `field` and `reason`. Diagnostics exclude raw field values. Invalid headers reject that file; an unrecoverable CSV syntax error rejects the remaining file while allowing other files to load. The CSV parser preserves ordinary field content while decoding standard CSV quoting.
- Updated `starter/apps/web/src/app/vehicle-list/vehicle-list.component.html` to use Angular's date pipe with `dd MMM yyyy`, producing dates such as `08 Apr 2026` without changing the source value.
- Kept the supplied CSV files unchanged, as required by R-4.1 and R-6.1. Malformed times are rejected rather than silently padded. Preserved the user's ADR and previous log entries. No unrelated features were implemented.

**Validation actually run:**

- Backend: Java 21 in the existing Maven Docker environment, running `org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.12:report`. Result: **103 tests, 0 failures, 0 errors, 0 skipped** — 98 validation cases, four dataset cases and the existing controller smoke test. Log: `starter/apps/api/target/issue-1-green-run.log`.
- JaCoCo: `VehicleCsvLoader` has **80/81 executable lines covered (98.8%)** and **36/40 branches covered (90%)**. The remaining uncovered executable line is the malformed-CSV-syntax rejection log. This measures the loader class, not whole-application or HTML-template coverage. Report: `starter/apps/api/target/site/jacoco/index.html`. Coverage tooling was invoked from the command line; no build dependency or configuration was changed.
- Frontend: `npm.cmd test -- --progress=false` with headless Chrome. Result: **2 tests passed**, checking `08 Apr 2026` and `01 Nov 2022` in the rendered Start Date cell and preservation of the original date value. Log: `starter/apps/web/.angular/issue-1-green-run.log`.
- The dataset checks compare every field of the accepted events and require a rejection diagnostic for each of the eight known malformed-time rows: personal 42 accepted, commercial 33, deregistered personal 95, deregistered commercial 56.
- `git diff --check` passed. Verified that the committed tests and source CSVs are unchanged and no files are staged. Generated test reports remain ignored. No commits, pushes or merges were performed.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 008 — 28 Oct 2026 — Diagnose stale page on port 4200

### 1. Task description

_User to complete._

### 2. User prompt(s)

```text
after update code, why the 4200 page no update
```

### 3. AI output and user assessment

**AI answer:**

The page on port 4200 is served by the existing Docker web container using an Angular production build and Nginx, rather than Angular's development server. The Compose configuration has no source bind mounts or watch configuration. Saving source changes therefore does not update the running application. Both the web and API containers were created approximately 26 hours before this check and still serve the earlier build. Explained that running `docker compose up -d --build` from `starter` rebuilds and updates both services; then refresh the page. A container restart alone does not build the changed source.

**Work produced and validation:**

- Read the assignment, current agent instructions, Compose configuration, frontend Dockerfile, package scripts, Nginx configuration, current template diff, and existing AI log. Observed the current issue branch and existing uncommitted implementation changes; left them unchanged.
- The initial restricted Docker and localhost requests were denied. Retried with approved elevated read-only access and successfully inspected the running containers and both HTTP endpoints.
- Confirmed `starter-web-1` maps port 4200 to port 80 and the HTTP response is served by Nginx. Confirmed `starter-api-1` maps port 8080 and the first vehicle still has `startDate` equal to `2022-01-11`, corresponding to the old incorrect interpretation of source value `01/11/2022`.
- Did not rebuild or restart containers, run tests, change production code, stage files, commit, push, or merge. Appended only this diagnostic log entry. The rebuild command is a recommended next step, not an action already executed.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 009 — 28 Oct 2026 — Build and run the updated application

### 1. Task description

To know how to build after code change

### 2. User prompt(s)

```text
help me build and run
```

### 3. AI output and user assessment

**AI answer:**

Rebuilt and started both Docker services. The application is running at `http://localhost:4200`, and the API is available at `http://localhost:8080/api/vehicles`. Verified the updated page displays `01 Nov 2022` and the API returns `2022-11-01`. Fixed a frontend build configuration problem encountered during the build; both frontend unit tests still pass. Changes remain unstaged and uncommitted for manual review.

**Work produced and validation:**

- Read the assignment, agent instructions, startup README, Compose configuration, Dockerfiles, Angular configurations and current AI log. Preserved the existing issue #1 changes and user log content.
- Ran `docker compose up -d --build` from `starter`. The first attempt failed because `tsconfig.app.json` included `src/**/*.ts`, which also included the committed component tests, while the application build intentionally had no Jasmine types. The errors identified test globals such as `describe`, `jasmine` and `expect`. The original failure is recorded in `starter/apps/api/target/build-and-run.log`.
- Verified that GitHub issue #1 remains open and the current branch is `issue-1-fix-date-parsing`, with the existing failing-test commit `b1693e7`. Added `"exclude": ["src/**/*.spec.ts"]` to `starter/apps/web/tsconfig.app.json`. This separates the application build from the existing test build; `tsconfig.spec.json`, test assertions and application behaviour were not changed in this turn.
- Repeated `docker compose up -d --build` successfully. Docker reported both images built and recreated and started `starter-api-1` and `starter-web-1`. Successful build output is recorded in `starter/apps/api/target/build-and-run-success.log`. Both containers remain running for the user.
- Reran `npm.cmd test -- --progress=false` with headless Chrome: **2 tests passed**. Log: `starter/apps/web/.angular/build-config-tests.log`. Backend tests were not rerun in this turn; the supplied API Dockerfile packages with `-DskipTests`, and the 103 passing backend tests are recorded in Entry 007.
- Confirmed both HTTP endpoints return **200**. The first API record has `startDate: 2022-11-01`, `endDate: 2032-01-01` and `time: 12:30:00`. Startup logs show structured rejections for the same eight malformed-time rows.
- Opened the rebuilt frontend in Chrome, verified the Start Date cell reads `01 Nov 2022`, and left the application tab open for review. This was a startup and date-display check, not a complete audit of the assignment's other requirements.
- Updated this log and the single build configuration file only. No staging, commits, pushes or merges were performed.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 010 — 29 Oct 2026 — Failing detail-view tests and ADR

### 1. Task description

based on issue 3 to create unit test and ADR

### 2. User prompt(s)

```text
1. Problem statement — what is wrong or missing, in user-visible terms

- add detail view feature for the TODO part for vehicle component

2. Root cause hypothesis — your best current theory (you may be wrong; that is fine, update the issue when you learn more)

- the feature is not implement yet

3. Acceptance criteria — specific, testable conditions that mean the issue is resolved

- able to pass in later to this page
- I prefer a pop up page, with currentpath/uuid, so easy to close and return to the state before click, and able to handle multiple different vehicle detail page, able to compare if needed
- it much usable on a 1280 x720 desktop browser, with responsive design (grid view here)
- need to update model, vehicle list page need to get all the data available in excel, then pass in the all field to the detail page based on the on click of specific row
- need to ensure the page pass R-6.2
- pass unit test

4. Requirement reference — which R-x.y this maps to, or discovered-defect if it does not map to a stated requirement
   R-1.6 Clicking a row or calendar entry must open a detail view showing all fields for that vehicle.
   R-2.3 Dates must be rendered as DD MMM YYYY (e.g. 08 Apr 2026).
   R-2.4 The UI must be usable on a 1280×720 desktop browser. Mobile responsiveness is a bonus.
   R-5.2 Test coverage must be sufficient to support the TDD constraint in §7. Aim for ≥70% line
   coverage on changed code.
   R-6.2 All user-supplied fields (uuid, brand, type, user_uuid) must be rendered using
   Angular’s safe-binding primitives (interpolation, property binding). Do not use innerHTML on
   untrusted data; DomSanitizer.bypassSecurityTrust* is acceptable only with a
   documented justification.

here my github issue 2, i need your help on

1. i have created the feature based on the issue 2
2. i need the unit test like mentioned in point 8, that able to not only handle the parse issue in backend but also check all csv data with R6.1 rules
3. it no need any code changes, need the failing test first
4. after done 1 to 3, update the ADR based on my github issue and the unit test done, dun add any additional no related on this issue fix info
```

### 3. AI output and user assessment

**AI answer:**

Prepared failing detail-view tests and updated the ADR, without changing production code. GitHub assigned this second issue **#3**, “Add detail view feature”; **#2** is the merged pull request for issue #1. Work is on `issue-3-add-detail-view-feature`, with the previous `issue-2-add-detail-view-feature` branch preserved. Final frontend result: **23 tests, 20 failed and 3 passed**. Backend regression result: **103 passed, no failures, errors or skips**. Review and manually commit the failing tests on the issue branch before requesting the implementation, as §§8 and 16 require.

**Work produced:**

- Read `assignment.md`, agent instructions, the current ADR, models, components, service, application configuration and existing tests. The working tree was initially clean. The model already contains all eight CSV fields plus category; the detail component is still `TODO`. No model change was needed to express the tests.
- The GitHub connector returned 404 for this private repository. Used the signed-in browser to find and read the open detail issue at `https://github.com/engjw/lta_assignment/issues/3`; its body matches the supplied acceptance criteria. Did not edit or post to GitHub.
- Confirmed the original issue-2 branch and `main` both pointed at merge commit `91d48eb`. Created `issue-3-add-detail-view-feature` from local `main` to follow §7's issue-number branch naming. Created no commits and did not rewrite history.
- Added `starter/apps/web/src/app/vehicle-detail/vehicle-detail.component.spec.ts`: 15 tests for all labelled fields/full UUIDs, delayed and changed input, repeated-UUID event values, clearing input, three date examples covering both date columns, literal rendering of markup in four untrusted fields, Close output, independent component instances, and grid layout at 1280×720 and 360×640.
- Added `vehicle-detail.test-support.spec.ts` in the same directory: fixture data, text/control helpers and a test-only iframe with a real CSS viewport. It mounts the actual component with its compiled styles; it does not implement the missing detail view. Layout assertions check grid columns, field geometry, horizontal overflow, desktop visibility and the Close control using maximum-length brand/type values.
- Added `starter/apps/web/src/app/vehicle-list/vehicle-list.popup.spec.ts`: four tests for the clicked row's full UUID URL, independent popup targets, unchanged list page/filter/record order, and feedback when the browser returns no popup handle. Browser opening is spied on; these tests do not create actual popups.
- Added `starter/apps/web/src/app/vehicle-detail/vehicle-detail.route.spec.ts`: one test using the real application configuration, requiring a detail route that renders all fields for a unique UUID. No replacement routes are installed in the test. Its current failure clearly reports the empty application route configuration.
- Added `starter/apps/web/src/app/vehicle.service.spec.ts`: one passing HTTP-boundary regression test proving the list response retains all nine model fields and distinct events with the same UUID. The backend response is a test fixture; the existing backend suite separately checks actual CSV ingestion.
- Updated `docs/adr/0001-vrls2-takehome.md` around this issue's confirmed popup design, existing model, selected-event ambiguity, safe rendering, responsive layout, trade-off and test evidence. Preserved a concise statement of issue #1 validation policy and the mandatory R-6.3 authentication stance. The latter remains planned work; no authentication implementation was added. No unrelated feature decisions were introduced.

**Actual validation and limitations:**

- Ran `npm.cmd test -- --progress=false` with headless Chrome. First run: 19 failures and 4 passes. Tightened the input-clearing test to require a visibly rendered record before clearing it, and changed the route test's missing-configuration failure from a routing exception to a direct assertion. Also added geometry assertions for all field values in the layout tests. Reran the final files: **20 failures and 3 passes** across 23 tests. The new tests account for 20 failures and one pass; the earlier two issue #1 display tests still pass. Log: `starter/apps/web/.angular/issue-3-red-run.log`.
- These are runtime assertion failures, not missing imports or TypeScript compilation failures. The 15 detail tests fail because the component renders `TODO`; four popup tests fail because row clicks do not open a browser window; the route test fails because no application routes are configured. The service data-preservation test passes because those fields already exist and are preserved.
- Ran the existing backend suite using Java 21 in `maven:3.9-eclipse-temurin-21`: **103 tests passed** (98 field/schema validation cases, four dataset cases, one controller smoke test). The dataset checks still cover all 234 source rows, expecting 226 accepted and eight rejected malformed-time rows. Log: `starter/apps/api/target/issue-3-csv-regression.log`. No CSV files or backend tests were changed.
- Component tests check safe literal rendering, not a complete security audit. Popup spies and separate component instances do not prove cross-window payload delivery, real popup closing/return behaviour, browser popup permission policy, or full-page visual usability. Those require integration/browser verification when the feature exists. No ≥70% implementation-coverage claim is made during this red-test stage.
- The route test uses a unique UUID. For repeated UUIDs, clicking must preserve the selected event; which event to show on a bare URL or refresh without that payload remains an explicit ADR question. Do not silently use the first matching event.
- Verified production files and existing tests are unchanged; all new code is in `.spec.ts` files excluded from the application build. Nothing was staged, committed, pushed or merged. The running application was not rebuilt or modified.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 011 — 29 Oct 2026 — Confirm detail-test scope and popup behaviour

### 1. Task description

_User to complete._
Make clarification to let codex answer more point to what I need

### 2. User prompt(s)

The user answered two clarification questions:

```text
Question: For issue #2, should the new tests focus on vehicle details, navigation, responsive layout and safe rendering (R-6.2)? Your point 2 repeats issue #1’s backend parsing and CSV validation scope.
Answer: Detail-view tests; rerun existing CSV tests

Question: For comparing vehicles, should each row open a separate browser popup at a URL ending in the vehicle UUID, leaving the list unchanged?
Answer: Separate browser popup per vehicle
```

### 3. AI output and user assessment

**AI answer:**

Applied those choices to the tests and ADR described in Entry 010: new tests target vehicle details and independent browser popups, while the existing CSV suite is rerun unchanged. Did not reinterpret “popup” as an in-page dialog or add new backend validation rules. Explained that the verified GitHub issue is #3 and used the matching branch. The results and test limitations are recorded in Entry 010.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 012 — 29 Oct 2026 — Review issue #3 tests before committing

### 1. Task description

know more about the unit test created by codex, and do review after that

### 2. User prompt(s)

```text
explain the unit tests you added for issue 3 and why they fail, so i can review before manually committing
```

### 3. AI output and user assessment

**AI answer:**

The 21 newly added issue #3 tests have 20 expected failures and one pass in the last recorded run. Including the two existing list-date tests, the frontend total is 23 tests: 20 failures and three passes. The existing 103 backend tests passed, including CSV validation and all four dataset checks. These are saved results from the previous test run; no tests were rerun for this explanation.

`vehicle-detail.component.spec.ts` contains 15 failing tests:

| Group | Tests | What is checked and why it currently fails |
| --- | ---: | --- |
| Complete fields and input behaviour | 5 | All eight CSV fields plus category, full UUIDs, delayed input without mutation, updated values for a different event with the same UUID, clearing a previously visible record, and independence of two detail instances. These are five test cases: the first combines the field checks, followed by delayed input, changed event, clearing, and independent instances. The template only renders `TODO`, so the expected values are absent. The clearing test fails its initial visible-record check; it does not establish that stale values are currently displayed. |
| Date display | 3 | Both start and end dates must render as `08 Apr 2026`, `01 Nov 2022` or `29 Feb 2024`, with unchanged source strings. Each example requires the formatted value twice. No dates are rendered yet; this is not a new backend parsing failure. |
| Safe text rendering | 4 | Supply markup directly in `uuid`, `brand`, `type` and `userUuid`. Require the complete literal text to be visible and no injected element to exist. The literal-text assertion fails because only `TODO` appears. These failures are not evidence of an existing XSS exploit; the no-injected-element assertion currently passes. |
| Close control | 1 | Clicking a labelled Close button emits the component's `close` output once and does not change its record. There is no button, so no close event is emitted. This does not test a real browser window closing. |
| Responsive grid | 2 | Mount the real component and its styles in 1280×720 and 360×640 iframes. Check multi-column desktop/single-column narrow layout, long values, no horizontal overflow, visible fields and Close control; desktop values must fit vertically. The current component has no fields, grid or Close button. |

`vehicle-list.popup.spec.ts` contains four failing tests: a row opens a same-origin `currentpath/full-uuid` popup without navigating the list; different vehicles use separate destinations/window targets; list page/filter/records/order are preserved without reloading the list; and a blocked popup gets visible feedback while the list stays usable. Current `open(vehicle)` only assigns `selected`; it never calls `window.open`. All four therefore fail the opening expectation. The state-preservation assertions already pass, and the blocked-popup case also lacks its expected alert. `window.open` is spied on and returns null in these tests, so they do not verify successful window creation or cross-window data delivery.

`vehicle-detail.route.spec.ts` contains one failing test using the actual application configuration and a mocked vehicle service. It requires `/<uuid>` to show the complete detail rather than the list or `TODO`. The current route configuration is empty, so the first routing assertion fails. The route test uses a unique UUID and does not resolve the multiple-events case or verify a real HTTP lookup.

`vehicle.service.spec.ts` contains the one new passing test. It supplies an HTTP response containing two events with the same UUID and verifies the service preserves all nine model fields and both records. The existing model/service already do this. This is useful regression protection, not a deliberately manufactured failure or a reason to change an already complete model.

`vehicle-detail.test-support.spec.ts` supplies fixture records, text/control helpers and the layout iframe. It adds no standalone test executions and no implementation of the feature.

Review the design choices before committing: the narrow viewport is specifically 360×640 with one column; desktop fields must fit within 1280×720; Close must be a labelled button; blocked opening must have visible alert feedback; and the route test expects Angular application routing. These make the acceptance criteria concrete but are not all exact requirements stated in the assignment.

The tests do not yet prove actual selected-row transfer into a popup, successful opening and closing of real windows, restoration of browser focus/scroll, full-page visual usability or complete R-6.2 compliance. They check literal rendering but do not inspect source code to prohibit `innerHTML` or sanitiser bypasses. The direct-link/refresh policy for a UUID with multiple events remains an ADR question; the component test only proves that supplied event values must be displayed. No ≥70% implementation-coverage claim is made. The user should review and manually commit the red tests on `issue-3-add-detail-view-feature` before production implementation, as §§8 and 16 require.

**Work produced and validation:**

- Read the assignment, current agent instructions, five test/support files, relevant production components/configuration, ADR and saved frontend/backend results.
- Confirmed the current branch and that no tracked application files or staged files have changed. Did not rerun tests, alter test assertions, change the ADR or production code, rebuild containers, stage files or commit.
- Appended this explanation to the AI log while preserving previous entries and user assessment fields.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 013 — 29 Oct 2026 — Document test and rebuild commands

### 1. Task description

_User to complete._
add command to readme file

### 2. User prompt(s)

```text
add mvn test, angular test, and docker build command after code change to readme
```

### 3. AI output and user assessment

**AI answer:**

Updated `starter/README.md` with an “After code changes: test, build and run” section. It documents `mvn test` from `apps/api`, Angular tests through `npm test -- --progress=false` from `apps/web`, and `docker compose build` followed by `docker compose up -d` from `starter`. Included the combined `docker compose up -d --build` alternative, service status check and browser refresh.

Added the Java 21 Maven Docker alternative for PowerShell, Chrome setup guidance and the Windows `npm.cmd` alternative. Explained that the CSV tests need the API working directory, Docker containers require rebuilding to pick up source changes, and Docker builds do not run these test suites. Retained the assignment's red-test review/manual-commit sequence before implementation.

**Work produced and validation:**

- Read the assignment, agent instructions, existing README, frontend test script, Compose configuration and relevant Dockerfile build commands. Checked the issue branch and preserved the existing tests and ADR changes.
- Checked the documented paths and commands against the repository configuration and previously executed commands. Ran `git diff --check`; no tests or builds were run for this documentation-only update.
- Changed only the README and appended this log entry in this turn. No files were staged, committed, pushed or merged.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 014 — 29 Oct 2026 — Implement issue #3, test and run Docker

### 1. Task description

_User to complete._

### 2. User prompt(s)

```text
now implement issue 3 and make all unit tests pass, then build and run with docker
```

### 3. AI output and user assessment

**AI answer and work produced:**

Verified a clean working tree on `issue-3-add-detail-view-feature` and the user's failing-test commit `e010d7f`. Verified GitHub issue #3 was open in authenticated Chrome after the connector and signed-out in-app browser returned 404. Read the assignment, agent rules, ADR, tests and implementation. The committed frontend tests reproduced **20 failures / 3 passes** before production edits. The first restricted run could not launch Chrome; rerunning with approved execution access worked.

Updated the ADR before implementation with the user's event-chooser decision. Added tests for selected-row handoff, independent popup storage, refresh, corrupt/changed cached records, chooser selection, missing/invalid vehicles, API errors and browser closing. Corrected a test typing error, then confirmed **30 failures / 3 passes**. Three added backend lookup tests also failed before implementation. These additions extend the committed issue #3 red baseline; they are not separately committed.

Implemented all-field safe-bound detail rendering, formatted dates, responsive grid, Angular UUID routing, separate browser popups, complete event handoff through each popup's session storage, opener detachment, Close and blocked/storage-error feedback. Added optional UUID lookup to the existing list endpoint to return all matching validated events independently of page size. Cached data is only a selection hint: the page displays matching validated API records. Direct URLs choose automatically only when exactly one event exists; multiple events require selection. Existing models already contained all fields, so no model or CSV changes were needed.

**Validation and final outcome:**

- All **34 Angular tests passed** with ChromeHeadless, including the added real HTTP service contract test. Frontend line coverage was **79/90 (87.77%)**; detail page **44/45 (97.77%)**, list component **24/33 (72.72%)**, service **5/6 (83.33%)** and the remaining changed frontend production files **100%**. These are instrumented TypeScript line figures, not visual or security-completeness measures.
- All **106 Maven tests passed**, including 103 existing CSV/controller regressions and the three new detail-lookup tests. JaCoCo controller line coverage was **13/16 (81.25%)**; existing CSV loader coverage remained **80/81 (98.77%)**.
- `docker compose up -d --build` succeeded, rebuilt both images and started `starter-api-1` and `starter-web-1`. HTTP checks verified the web page returned 200 and the new API UUID lookup returned the expected complete vehicle event. The first restricted HTTP check was denied local socket access; an approved rerun succeeded. The existing list pagination behavior was not changed by this issue.
- Browser checks confirmed separate popups for two UUIDs, all nine displayed fields, formatted dates, refresh retaining the same event, and a direct UUID link resolving through Nginx. Chrome mouse/screenshot automation timed out on a popup; keyboard Close subsequently removed that popup while another popup and the list remained. Further checks compared the list on Page 2 with the Deregistered filter before/after opening: records/order/page and the active filter were preserved (accessibility focus marking changed, as expected).
- Inspected complete-page screenshots at 1280×720 and 360×640 in the in-app browser; the desktop fields fit and the narrow layout used one column without horizontal overflow. Temporary viewport overrides were reset. The dataset has no repeated UUIDs, so chooser/selected-event ambiguity is exercised by fixtures rather than claimed as a real-data browser check. Browser focus restoration and all browser popup-policy variations were not exhaustively tested.
- Saved run output in ignored `.angular/issue-3-committed-red.log`, `.angular/issue-3-extended-red.log`, `.angular/issue-3-green.log`, API `target/issue-3-lookup-red.log`, `target/issue-3-backend-green.log`, and `target/issue-3-docker-build.log`; coverage reports are under the existing ignored output directories.
- Updated the issue-focused ADR with the implementation, decision and evidence. All prepared changes remain unstaged and uncommitted for manual review. No GitHub issue text was posted or rewritten by the agent.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 015 — 29 Oct 2026 — Direct URL event selection

### 1. Task description

_User to complete._

### 2. User prompt(s)

Question: For a vehicle URL opened directly without a clicked row, a UUID can match several status events (R-4.2/R-4.3). Should the page show an event chooser or the latest event? Popups opened from the list will always show the exact row you clicked.

User answer:

```text
Show an event chooser (recommended)
```

### 3. AI output and user assessment

**AI answer:** Recorded this decision in the ADR and implemented an event chooser for direct URLs with multiple events. A clicked-row snapshot selects its exact matching API event and survives refresh in that popup. No arbitrary first or latest event is selected for ambiguous direct links.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 016 — 29 Oct 2026 — Remove redundant navigation and refine detail ADR

### 1. Task description

_User to complete._

### 2. User prompt(s)

```text
1. no need back to vehicle list button, because close also will go back
2. update ADR, based on the update changes did, and with suggestion below
   1. the pop up url better with a unique UUID for that record, in future with proper DB structure
   2. can still stick to vehicle UUID, and add one more feature, which will use api to get all the records and can based on tab to choose which detail to display in pop up detail page, default is open that specific record clicked
   3. add remark there, currently direct open with link if not from record list, it always open the first event, these can be solve with above 1 and 2 idea
```

_HTML whitespace entities normalized above; wording preserved._

### 3. AI output and user assessment

**AI answer and work produced:**

Removed the Back to vehicle list link, unused RouterLink import and link styling from `vehicle-detail-page.component.ts`. Kept the existing Close behavior. This is a small review refinement of the existing issue #3 implementation, whose failing-test commit is `e010d7f`; no new feature or tests were introduced for the link removal.

Updated `docs/adr/0001-vrls2-takehome.md` to describe the current implementation and the user's future proposals: persistent status-event record UUIDs with a proper database, or vehicle UUID URLs with API-backed event tabs and the clicked record selected by default. Retrieval of all events for one vehicle already exists; record UUIDs and tabs remain proposals, not completed work. Documented that shareable selection of a specific event still needs a stable identifier.

Corrected the proposed first-event remark against the code and tests: the current detail page restores a matching saved selection, automatically shows a sole event, or offers a chooser for multiple events. Only the older single-record API lookup uses `findFirst`; the detail page does not call it. No selection behavior was changed to make the remark true. Documented that Close returns from script-opened popups by closing them; directly opened browser tabs may require the browser's own close control.

Read the assignment and agent instructions, confirmed the issue branch/red commit, and verified issue #3 remains open in authenticated GitHub. Preserved the ADR's required security/threat-model and R-6.3 authentication stance and kept the record focused on issue #3.

**Validation:** All **34 Angular tests passed** after the link removal (`.angular/issue-3-close-only.log`). Backend code was unchanged and Maven tests were not rerun. `docker compose up -d --build --no-deps web` succeeded; both containers are running. Verified the rebuilt detail URL shows all fields and Close with no Back to vehicle list link. Build output is saved in ignored `.angular/issue-3-close-only-build.log`. The ADR is 455 words, and `git diff --check` passed. Changes remain unstaged and uncommitted.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 017 — 29 Oct 2026 — Consolidate the shared assignment ADR

### 1. Task description

_User to complete._

### 2. User prompt(s)

```text
I think the ADR should with all issues, not one issue one ADR, help to combine it and and give exact format in assignmenet.md, and keep everything concise and clear, no redundant info
```

### 3. AI output and user assessment

**AI answer and work produced:**

Confirmed that assignment §9 requires a single shared, one-page ADR at `docs/adr/0001-vrls2-takehome.md`, rather than replacing it with an issue-specific record. Reviewed the current ADR, the merged issue #1 version, relevant implementation and AI-log decisions. Verified GitHub's all-issues view contains issue #1 (Date Time format issue, completed) and issue #3 (Add detail view feature, open). Used a separate browser tab so the user's existing new-issue page was not navigated away from or edited.

Rewrote the shared ADR in 473 words using exactly the six requested section headings: Context; Conflicts found in §5; Data model; Security & threat model; One explicit trade-off; Open questions. Context has four sentences. Combined strict CSV ingestion/rejection and date display from issue #1 with event preservation, popup selection, responsive details and safe rendering from issue #3. Retained the user's future record-UUID/database and API-backed tab proposals and the unconfirmed brand/type bounds. Removed test-run history, coverage figures and repeated implementation notes from the ADR; their evidence remains in this log.

Kept current implementation distinct from planned work: authentication/authorisation closure, accepted-record provenance and ingestion times, and uniform API error envelopes remain outstanding. Documented the actual direct-link chooser behavior without repeating the incorrect always-first-event claim. Preserved the explicit R-6.3 production closure plan and a scoped threat model.

**Assignment reminder:** neither existing issue explicitly surfaces a §5 requirement conflict. The ADR identifies R-1.4 versus R-2.1 as conflicting default-sort rules and records that no precedence rule is approved. The user must raise the conflict in their own words and decide the rule before related implementation (§§7, 16). This documentation edit does not satisfy that GitHub issue requirement and does not invent an approved resolution or post an AI-written issue.

**Validation and repository state:** Documentation only; no application/test/CSV changes, test runs or Docker rebuilds. Checked the six headings against assignment §9, the four-sentence context, word count and `git diff --check`. The user had already staged changes at the start of this turn. Preserved that staged content and left the new ADR and AI-log edits unstaged; no commits, pushes or merges.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 018 — 29 Oct 2026 — Review issue #5 before writing tests

### 1. Task description

_User to complete._

### 2. User prompt(s)

```text
1. Problem statement — what is wrong or missing, in user-visible terms
- R-1.4 and R2.1 both with default sorting but on different columns
- R-1.7 and R3.2, if with so huge of vechicle data, how the admin to view it in calendar view
- R-1.8, R-3.1, R-3.2 and R-3.3, if search is performed on client side, which mean have to call all the data to the frontend and do the search, so it impossible with a list view with 2 seconds time to interactive

2. Root cause hypothesis — your best current theory (you may be wrong; that is fine, update the issue when you learn more)

3. Acceptance criteria — specific, testable conditions that mean the issue is resolved
- R-1.4 and R2.1 can combine, sort with R.2.1 first then follow up by R-1.4
- R-1.7 and R3.2, remove calendar view but add date time filter (start and end) in list view
- R-1.8, R-3.1, R-3.2 and R-3.3, the search will as a payload to api, and because the records is huge, should set a limit like maximum records pull is 2,000, so the filter and sorting can work well also. Maybe need create more search option, like the datetime filter, seat type, so only can perform better with within 2,000 records.
- pass unit test

4. Requirement reference — which R-x.y this maps to, or discovered-defect if it does not map to a stated requirement
R-1.4 The list view must default to sorting by registration date, descending (newest first).
R-2.1 All tabular data displayed to the admin must be sorted alphabetically by vehicle UUID by default, to give a stable visual order across sessions.
R-1.7 The calendar view must show every vehicle on its corresponding day, and each vehicle must appear exactly once per day cell.
R-3.2 The system must handle the full historical dataset, expected to grow to ~250,000 vehicle records by 2027.
R-1.8 A search box must allow free-text search by vehicle UUID prefix or brand.
R-3.1 All filtering, sorting, and search must be performed on the client so that admins get instant feedback with no network round-trip.
R-3.3 Time-to-interactive on the list view must be under 2 seconds on a standard office network.

here my github issue 5, i need your help on

1. i have created the feature based on the issue 5
2. check on all my problem and suggestion, is it ok to proceed, if yes then continue below steps, else just stop and give me your suggestion
3. i need the unit test like mentioned in point 8
4. it no need any code changes, need the failing test first
5. after done 1 to 3, update the ADR based on my github issue and the unit test done, dun add any additional no related on this issue fix info
```

_Markdown escape characters and line-continuation markup normalized; wording preserved._

### 3. AI output and user assessment

**AI answer and recommendation:**

The issue needs revision before tests. Honoured the user's conditional instruction to stop and provide suggestions instead of proceeding with tests or ADR changes. Verified GitHub issue #5, “Requirement conflicts,” is open and contains the supplied analysis. It now explicitly surfaces the genuine R-1.4/R-2.1 sorting conflict; the earlier warning that no conflict issue exists is no longer applicable.

- Sorting: two primary default orders cannot both hold in general. UUID ascending followed by date descending as a secondary key only orders dates within each UUID. Recommended registration date descending with UUID ascending as a tie-breaker, explicitly prioritising R-1.4 over R-2.1. Keeping UUID primary is also a possible deliberate decision, but it must acknowledge the departure from global newest-first ordering rather than claim both requirements are fully satisfied.
- Calendar: R-1.7 plus dataset size is a usability/performance concern, not by itself a logical contradiction. Removing the calendar violates the explicit two-view requirement R-1.1. Suggested keeping the monthly view with per-day counts and expandable/paginated entries, with every applicable vehicle reachable. Any month-scoped server query belongs to the documented R-3.1 exception. Clarify the calendar/current-status/history interpretation with R-4.4 and R-4.5 rather than dropping event history.
- Search and scale: the performance concern is reasonable, but “impossible under two seconds” is unproven without a defined browser/network benchmark. Server-side queries can be a justified resolution, but explicitly depart from R-3.1. Recommended applying search, all filters and ordering across the full dataset before returning 50-record pages (R-1.3), with total-match/continuation information. A hard 2,000-record result universe can hide valid matches or newer records and does not provide globally correct client sorting/filtering. If retained, 2,000 should be a transfer/batch bound with a way to reach further results, not a silent truncation of searchable history.
- Acceptance criteria need the user's root-cause hypothesis, exact primary/secondary sort order, date-field/range/timezone semantics, text-search matching rules, and complete pagination behavior. Include missing references such as R-1.1, R-1.3, R-1.5, R-4.4 and R-4.5 where the decision affects them. Unit tests can check sorting ties, combined filters, boundaries and matches beyond record 2,000; a separate browser/performance test on 250,000 records is needed for the two-second criterion.

**Assignment handling:** The recommendations above are AI review suggestions for the user to assess and express in their own words (§7), not a replacement issue submitted as their analysis. Before related implementation, the shared ADR must record the selected rules and requirement departures (§§9, 16), and the user must review and commit the failing tests (§8). No decision was recorded as approved.

**Work and validation:** Read the assignment, agent rules, shared ADR, current list/backend code and recent log. Confirmed a clean working tree on `issue-5-requirement-conflict` at `e84e078` before this log edit. No production code, tests, CSVs or ADR changes; no test run, benchmark, Docker build, staging, commit, push, merge or GitHub write. Updated only this mandatory AI usage log and checked the diff for whitespace errors.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 019 — 29 Oct 2026 — Issue #5 selected scope

### 1. Task description

_User to complete._

### 2. User prompt(s)

```text
1 and 2 follow my suggestion, 3 just put in ADR first
```

### 3. AI output and user assessment

**AI answer and work produced:**

Followed the user's original UUID-ascending primary order, registration-date-descending secondary order, and list date filters instead of a calendar. This overrides the alternative sorting/calendar suggestions in Entry 018. Treated the API search payload and proposed 2,000-record limit as ADR-only, with no tests or implementation for that proposal. Continued the original instruction to prepare failing tests without production code changes.

Explained that UUID-primary ordering departs from R-1.4's global newest-first order, and replacing the calendar departs from R-1.1, R-1.7 and R-4.4. The shared ADR now records those decisions rather than claiming all conflicting requirements are satisfied. The calendar/scale concern is labelled a usability concern, not a logical contradiction. API search, complete pagination, a limit that does not hide history, and a measured two-second performance target remain unresolved proposals. The current API returns one page: the new component tests cover supplied records, not global sorting/filtering of the entire history.

Asked whether the date bounds should operate on `startDate + time` or the separate CSV fields. The user's reply and resulting date contract are recorded in Entry 020.

Updated `docs/adr/0001-vrls2-takehome.md` using the six exact §9 headings and a four-sentence Context. Preserved issue #1 ingestion/date-format decisions, issue #3 popup/event decisions, security controls, the explicit R-6.3 production closure plan and outstanding security work. No GitHub issue edit was made; the user should add the clarified scope and their missing root-cause hypothesis in their own words (§7).

**Assignment handling:** issue #5 was verified open during the preceding review; work remains on `issue-5-requirement-conflict`. The shared ADR addresses the material conflicts as required by §§9/16. Under §§8/16, the user must review and manually commit the red tests before production implementation; that commit has not been created or claimed. Test results and files are recorded in Entry 020. All prepared changes remain unstaged and uncommitted.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 020 — 29 Oct 2026 — Separate CSV date filters and failing tests

### 1. Task description

_User to complete._

### 2. User prompt(s)

In response to the question:

```text
For the list’s “start” and “end” date-time filter, should both bounds select records by startDate + time, or should they filter the separate CSV start_date and end_date fields?
```

User's answer:

```text
Separate start_date/end_date filters
```

### 3. AI output and user assessment

**AI answer and work produced:**

Used the separate fields as requested. Explicitly stated the test contract for review: optional, inclusive `start_date >= Start date` and `end_date <= End date`; both conditions combine with AND and the selected status. These CSV fields are dates, so the controls use date-only precision; the separate `time` field does not affect matching. The user selected the fields; the inclusive comparison rules are the assistant's stated interpretation, not an additional quoted user instruction.

Added eight component tests in `starter/apps/web/src/app/vehicle-list/vehicle-list.query.spec.ts`:

1. Full UUID ascending takes precedence over a newer date on another UUID, using UUIDs with identical displayed prefixes.
2. Repeated-UUID events sort by start date descending across months/years, preserving all events and source values/order.
3. The start-date lower bound includes equality, excludes earlier dates, and does not compare end date or time.
4. The end-date upper bound includes equality, excludes later dates, and does not reuse start date for both filters.
5. Both date conditions combine with the selected status and retain UUID/date order.
6. Either filter can be cleared independently; clearing both restores all supplied events without rewriting them.
7. A valid filter with no matches removes stale rows; clearing restores the records.
8. Opening a filtered row transfers the exact event while preserving both date controls and list order.

Tests interact with labelled controls and rendered rows. The six filter tests currently stop after a failed assertion identifying a missing control, avoiding unrelated null-access errors; their deeper behavior assertions will run once controls exist. The calendar is already absent, so no artificial failing removal test was added. The ADR records that list-only choice as a requirement departure.

**Actual validation:** ran `npm.cmd test -- --progress=false` from `starter/apps/web` using headless Chrome. Exit code 1, **42 executed: 8 failed, 34 passed**. Both sorting tests fail because current rows retain input order; the other six fail because labelled start/end date filters do not exist. All 34 existing tests pass. Full output is in the ignored local `.angular/issue-5-red.log`. No backend tests were changed or rerun, and no coverage, full-history correctness or performance claim is made. Checked the ADR headings and `git diff --check`.

**Scope and next step:** only the new test file, shared ADR and AI usage log were edited. No production code, CSV, build, Docker, staging, commit, push, merge or GitHub write. Review and manually commit the failing tests on the issue branch before implementing (§§8/16); the remaining API/scale proposal is documentation only.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 021 — 29 Oct 2026 — Explain issue #5 failing tests for review

### 1. Task description

_User to complete._

### 2. User prompt(s)

```text
explain the unit tests you added for issue 5 and why they fail, so i can review before manually committing
```

### 3. AI output and user assessment

**AI answer and work produced:**

Reviewed `vehicle-list.query.spec.ts`, the current list implementation/template and the saved `starter/apps/web/.angular/issue-5-red.log`. Explained the eight tests individually: full UUID ascending before dates; newest start date within a repeated UUID; inclusive start-date lower bound; inclusive end-date upper bound; both dates plus selected status; clearing either/both filters; empty results and recovery; and opening the exact filtered event without losing filters/order. Explained that the tests use sample service responses and the real Angular component/template rather than reading CSV files or calling the backend. Source-array/value preservation is also asserted by the sorting and clearing tests.

The recorded run executed 42 tests: 8 new failures and 34 existing passes. Test 1 expects `Zulu, Alpha` (full UUID order) but receives `Alpha, Zulu` (input order). Test 2 expects `Newest, Middle, Oldest` but receives `Oldest, Newest, Middle`. Current `filteredVehicles()` only applies status filtering and does not sort. Tests 3–8 fail at their first missing labelled date control; their later behavior assertions have not yet executed. The assertion fails before the guard returns, so those tests are failures, not skipped tests. Adding controls alone will not satisfy the complete test expectations.

Highlighted the review contract: start date is an optional inclusive lower bound on CSV `start_date`, end date is an optional inclusive upper bound on CSV `end_date`, and both combine with AND. Neither compares the separate `time` field. UUID-primary ordering follows the user's selected R-2.1 precedence and is not global newest-first compliance with R-1.4.

Clarified limits: these tests cover records supplied to the component, not sorting/filtering across all API pages. No API-search/2,000-record-limit tests were added because that proposal is ADR-only; no 250,000-record benchmark, two-second performance proof, new CSV validation tests or calendar-removal test is claimed. The calendar is already absent and its requirement departure is recorded in the shared ADR.

**Validation and workflow:** This turn reviewed the existing test results; it did not rerun tests, change production/tests/ADR, build, stage or commit. Updated only this AI usage log and checked whitespace. Changes remain unstaged on `issue-5-requirement-conflict`. Reminded the user to review and manually commit the failing tests before production implementation, as required by §§8/16. No user approval or assessment was assumed.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._

## Entry 022 — 29 Oct 2026 — Implement issue #5 and run Docker

### 1. Task description

_User to complete._

### 2. User prompt(s)

```text
now implement issue 5 and make all unit tests pass, then build and run with docker, and 2000 record remain in ADR first
```

### 3. AI output and user assessment

**AI answer and work produced:**

Verified the clean issue branch `issue-5-requirement-conflict` and the user's failing-test commit `86b9bd1` (`add unit test and update ADR`) before production changes. It contains the eight issue #5 tests and documentation, with no production implementation. The earlier recorded run demonstrated all eight failures. The GitHub connector returned 404 for this private repository; verified issue #5, “Requirement conflicts,” remains open through the signed-in browser. No GitHub content was edited or posted.

Implemented the reviewed scope in `vehicle-list.component.ts`, `.html` and `.css`: full UUID ascending, then start date descending within each UUID; optional inclusive start-date lower and end-date upper bounds; combination with the existing status filter; clearing either date; and preservation of source events and popup/list state. Date comparisons use validated ISO dates without timezone conversion. Filtering produces a new array before sorting. The inputs use Angular property/event bindings with associated labels, and the filter layout wraps. Added an empty-results message and a visible current-page scope notice.

Updated the shared ADR's implementation status and current-page limitation. Preserved the user's sorting/calendar decisions, security/authentication stance and unresolved work. Server-side search and the proposed 2,000-record limit remain ADR-only: no API query/cap, backend or CSV changes. The application still has no calendar. Full-history sorting/filtering and the existing 49-record pagination defect remain unresolved; no complete issue #5 scale/performance compliance is claimed.

**Actual tests and correction:**

- First Angular implementation run: 41 passed, 1 failed. The filtered-popup test caught the start-date control displaying an empty value after `ngModel` initialization. Replaced the two `ngModel` bindings with direct property/input-event bindings and removed the unnecessary forms import. Kept the committed tests unchanged.
- Final Angular command: `npm.cmd test -- --progress=false --code-coverage`, with Chrome configured. **42 tests passed**, exit 0. Overall measured line coverage is **91.2% (83/91)**; the changed list component has **82.35% (28/34)** line coverage. These measurements cover TypeScript, not CSS/layout or a performance benchmark.
- Backend: ran `mvn -B -ntp -Dmaven.repo.local=/workspace/apps/api/target/maven-repository test` in the Java 21 Maven Docker image with the starter mounted at `/workspace`. **106 tests passed, 0 failures/errors/skipped**, exit 0, including CSV validation/dataset and detail lookup tests.
- Local ignored evidence: `starter/apps/web/.angular/issue-5-green.log` (initial failure), `issue-5-green-final.log` (final pass), the frontend coverage report, and `starter/apps/api/target/issue-5-regression.log`.

**Build and running-app verification:**

Ran `docker compose up -d --build` from `starter/`; both images built and both containers started successfully. `docker compose ps` reported `starter-api-1` and `starter-web-1` running. Frontend `http://localhost:4200` and API `http://localhost:8080/api/vehicles?page=0` both returned HTTP 200; the API page contained 49 records. Build output is in the ignored `starter/apps/web/.angular/issue-5-docker.log`.

Verified the running browser UI at 1280×720: setting the start bound to 1 Dec 2022 reduced 49 rows to three December matches; adding an end bound of 1 Jan 2022 showed the empty-results message; clearing the end bound restored three matches. No horizontal overflow was detected from DOM geometry. Clearing both restored 49 rows in full-UUID ascending order. Both date controls were empty afterward. Reset the temporary viewport override and left the app open. Browser screenshot capture timed out twice, so no screenshot artifact or visual screenshot inspection is claimed. This smoke check does not establish the 250,000-record/two-second requirement.

**Workflow:** checked `git diff --check`; production changes, ADR and this log remain unstaged and uncommitted for manual review. No tests were weakened, and no stage/commit/push/merge was performed. The red-test commit remains intact for the required §8 history.

**User assessment — what you kept, refined, or rejected, and why:**

_User to complete._

### 4. Where AI accelerated you vs where you overrode it

_User to complete._
