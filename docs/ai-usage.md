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
