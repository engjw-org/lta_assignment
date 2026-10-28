## Context

The start date is not shown in the required format. The backend reads CSV dates using the wrong date format. Some CSV rows also have the wrong time format.

## Decision

- Show dates on screen as `DD MMM YYYY`.
- Check all CSV rows before sending data to the frontend. Check missing fields, data types, text lengths, and allowed values.
- Accept times only in `HH:mm:ss` format. For example, accept `09:00:00` and reject `9:00:00`.
- Skip invalid rows and record the file name, row number, field, and reason.
- Keep the original CSV files unchanged.

The proposed maximum lengths are 100 characters for brand and 50 for type. These limits need confirmation because the assignment does not give exact limits.

## Requirement conflicts

The CSV date format and screen date format are different on purpose. 

## Data model

Each CSV row is one vehicle status event. The UUID identifies the vehicle.

Keep every valid event, including repeated UUIDs, so history is not lost. Record the source file, row number, and loading time. Later, use the latest event to show the vehicle’s current status.

Keep data in memory for this assignment.

## Security

Treat CSV data as untrusted and validate it before use. Display values using Angular’s safe bindings. Keep secrets out of the repository and avoid showing internal error details.

Allow access without login only in an explicitly enabled local development mode. Before production, require valid login, check admin permission on every API request, and block startup when login settings are missing.

These are planned controls, not completed work. Protection against a compromised server is outside this assignment’s scope.

## Trade-off

Strict validation means the eight rows with invalid times will not appear until corrected at the source. This avoids showing invalid data.

Data kept in memory will be lost when the application restarts.

## Open questions

- What are the correct maximum lengths for brand and type?
- Who should review and correct rejected rows?
- Which date should decide the latest status event?
- Which login system should production use?