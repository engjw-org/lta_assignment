# VRLS2 architecture

Current implementation, arranged as three layers. Cross-column arrows represent requests, responses and data access; the data layer runs within the API process or its files, not a separate database service.

```mermaid
%%{init: {"theme": "base", "themeVariables": {"fontFamily": "Arial, sans-serif", "fontSize": "14px", "primaryColor": "#ffffff", "primaryTextColor": "#0f172a", "primaryBorderColor": "#475569", "lineColor": "#64748b", "textColor": "#0f172a", "nodeTextColor": "#0f172a", "edgeLabelBackground": "#f8fafc", "titleColor": "#0f172a"}, "flowchart": {"wrappingWidth": 360, "nodeSpacing": 24, "rankSpacing": 32}}}%%
flowchart LR
  subgraph FRONTEND["FRONTEND · Angular / nginx · localhost:4200"]
    direction TB
    LIST["Search form and list<br/>UUID prefix / brand, start/end dates, seat type<br/>Submit search; show matches and cap notice<br/>Local status filters and 50-row pages"]
    POPUP["Vehicle UUID popup<br/>Uncapped API history; exact event or chooser<br/>All fields plus source file, row and ingestion UTC<br/>Dates: DD MMM YYYY · R-2.3 / R-6.4"]
    BINDING["Browser trust boundary · R-6.2<br/>Angular interpolation and property binding<br/>Session storage is only a selection hint<br/>Render API values; detach popup opener"]
    CALENDAR["Calendar decision · R-1.1 / R-1.7 / R-4.4<br/>List with date search replaces calendar<br/>Usability choice for R-3.2; calendar unmet"]
    LIST -->|Row click| POPUP
    POPUP -->|Safe display| BINDING
    BINDING ~~~ CALENDAR
  end

  subgraph BACKEND["BACKEND · Spring Boot · localhost:8080"]
    direction TB
    GUARD["Startup boundary · R-6.3<br/>Only the sole dev profile may start anonymously<br/>All other profiles are blocked<br/>Production login + admin authorisation deferred"]
    INGEST["Startup ingestion · VehicleCsvLoader<br/>Read four CSVs; validate before accepting · R-6.1<br/>Schema, presence, types, lengths, allow-lists<br/>Reject without rewriting; attach audit · R-6.4"]
    API["VehicleController / VehicleService<br/>POST search; GET list / UUID history / event<br/>Validate search criteria; search all valid events<br/>UUID ASC, then start date DESC · R-2.1 / R-1.4<br/>First 2,000 + totalMatches + truncated<br/>Server search departs from R-3.1 for R-3.2 / R-3.3"]
    ERRORS["Error boundary · R-6.6<br/>Controller advice and servlet error handler<br/>Generic code / message with HTTP status<br/>Internal diagnostics stay in server logs"]
    GUARD ~~~ INGEST
    INGEST -->|Accepted events with audit| API
    API -->|Failure| ERRORS
  end

  subgraph DATA["DATA · Source files / process memory / configuration"]
    direction TB
    CSV["Source of truth · R-4.1<br/>personal.csv / deregpersonal.csv<br/>commercial.csv / deregcommercial.csv<br/>One row represents one vehicle status event"]
    MEMORY["In-memory events and audit trail · R-6.4<br/>Retain duplicate UUIDs and all valid events<br/>audit: sourceFile, sourceRow, ingestedAt<br/>Basename, physical line, UTC ingestion instant<br/>Preserved through every API; no database"]
    LOGS["Server logs<br/>Rejected CSV: source, row, field, reason · R-6.1<br/>No raw CSV values in rejection entries<br/>Internal exception diagnostics · R-6.6"]
    CONFIG["Configuration / secret source · R-6.5<br/>Compose selects dev; ports bind to localhost<br/>No credentials required or stored in repository<br/>Future secrets: environment or mounted files"]
    CSV ~~~ MEMORY
    MEMORY ~~~ LOGS
    LOGS ~~~ CONFIG
  end

  FRONTEND <-->|HTTP / JSON| BACKEND
  BACKEND <-->|Ingest / store / read| DATA

  classDef default fill:#ffffff,stroke:#475569,color:#0f172a
  style FRONTEND fill:#dbeafe,stroke:#1d4ed8,color:#0f172a
  style BACKEND fill:#ccfbf1,stroke:#0f766e,color:#0f172a
  style DATA fill:#fef3c7,stroke:#92400e,color:#0f172a
```

The 2,000-result cap limits complete history access (R-4.5), although UUID detail lookups are uncapped. The two-second target (R-3.3) remains unmeasured. Audit data is held in memory; future persistence must retain it alongside events. Decisions and the production authentication closure plan are recorded in [ADR 0001](adr/0001-vrls2-takehome.md).
