# Agent instructions

## Assignment reference

- Read `assignment.md` before working on this assignment. It is the source of truth for requirements, workflow, and deliverables.
- Whenever something is unclear, consult the relevant section of `assignment.md` first. Do not invent requirements or silently resolve contradictions. Explain any remaining uncertainty to the user, reference the relevant `R-x.y` requirements, and record decisions in the required issue and ADR when applicable.
- Build on the supplied Angular and Spring Boot starter, as required by §3. Do not replace it with a newly scaffolded application.

## User review and manual commits

- The user reviews changes and commits manually. Leave prepared changes unstaged and uncommitted by default.
- Do not stage files, create or amend commits, rewrite commit history, push, or merge unless the user explicitly requests that specific action. An explicit request to undo commits authorises that undo only.
- Requests to implement a feature, write tests, prepare documentation, or follow the assignment's workflow do not by themselves authorise committing.
- For §8, prepare and run the failing tests, report their actual results, and remind the user to commit them on the issue branch after review. Verify that the failing-test commit exists before adding the related production implementation. Do not create that commit on the user's behalf unless explicitly requested.

## Record every user prompt (§11)

- Maintain `docs/ai-usage.md` throughout the work, including documentation, planning, questions, reviews, and implementation assistance.
- Record every substantive user prompt, including follow-ups, corrections, and changed constraints. Do not treat environment metadata, tool output, or system instructions as user prompts.
- Add a numbered entry for each prompt and response. Related entries may reference the same issue or task. Update the log before finishing each response; do not reconstruct missing history from guesses.
- Use the four numbered fields required by §11:
  1. **Task description — user to complete.** Leave this for the user.
  2. **User prompt(s) — agent to complete.** Preserve the actual prompt, preferably verbatim. If abridged, retain its context, constraints, and non-goals. Redact secrets rather than storing them, and mark the redaction.
  3. **AI output and user assessment — shared.** Record the assistant's answer and the work actually produced, including relevant files, decisions, validation results, and limitations. Keep the agent's account separate from the user's assessment of what they kept, refined, or rejected and why. Leave that assessment for the user unless they explicitly provide it.
  4. **Acceleration and overrides — user to complete.** Leave this for the user.
- Preserve the user's existing content, especially fields 1 and 4 and their additions to field 3. Append new entries instead of overwriting history.
- Never invent prompts, results, completed checks, acceptance decisions, or reasons for keeping or rejecting AI output. Record failed or unfinished work honestly. Do not claim that the user reviewed or accepted something without evidence.

## Disqualification reminders (§16)

Before acting on a prompt or making a relevant change, check whether it could trigger any of these risks. If it could, remind the user promptly, identify the specific §16 item and supporting assignment section, and explain the compliant next step. Repeat reminders when a new risk arises or remains directly relevant, rather than adding an unrelated checklist to every answer.

| Disqualification risk | Required handling |
| --- | --- |
| Code changes without a corresponding Issue | Verify an open GitHub Issue exists for the work before changing code (§7). Follow the provided issue template, including requirement references or `discovered-defect`. |
| Any commit to `main` other than the initial baseline starter commit and merged PRs | Check the branch before committing. After the baseline, use a fresh `issue-<number>-<short-slug>` branch from `main`, one branch per issue, and a PR referencing `Closes #<number>`. Merge with a merge commit; do not squash away the required history (§7). |
| Production code added without a failing test first | Write and run the relevant test to demonstrate the expected failure. The user reviews and commits the failing test manually; verify that commit before adding production code. Preserve the red → green → refactor history (§8). Never fabricate a red test or retroactively present tests as having been written first. |
| No GitHub Issue surfacing a requirement conflict in §5 | Ensure at least one issue explicitly identifies a real conflict with all relevant requirement IDs before implementing code related to that area (§§6–7). |
| An ADR that does not address the material conflicts raised | Before implementation, create or update `docs/adr/0001-vrls2-takehome.md` to explain the material conflicts and the decision rule adopted for each. Keep it to one page (§9). |
| An ADR missing security / threat-model discussion or the R-6.3 authentication stance | Include both the security / threat model and an explicit authentication and authorisation stance. A dev-only bypass needs a documented production closure plan; a bare stub or TODO is insufficient (§§5.6, 9). |
| No AI usage log, or an obviously fabricated log | Maintain the truthful, prompt-by-prompt log described above. Flag missing information for the user instead of inventing it (§11). |
| AI-generated issues passed off as the user's own analysis | Help edit or challenge analysis supplied by the user. Clearly identify AI suggestions, and remind the user that verbatim or lightly edited AI-generated issue text cannot be submitted as their own analysis. Do not invent the user's reasoning or submit AI-written analysis on their behalf (§7). |

- Do not silently bypass these hard gates. When a prerequisite is missing, explain it and continue with safe preparation that does not violate the assignment.
- Documentation-only issues do not require tests (§8), but committed documentation must still follow the issue branch and PR workflow (§7). Local preparation does not authorise a direct commit to `main`.
- If a requested repository check cannot be verified, say so; do not assume an issue, PR, failing test commit, or ADR already exists.
