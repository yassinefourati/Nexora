# Use-case scaffolding: spec-driven generation for backend and frontend

Status: proposal (2026-09-13). Not started. Depends on the Modulith target shape from
[ADR 0004](adr/0004-modular-monolith-spring-modulith.md) and the phases in
[backend-architecture-plan.md](backend-architecture-plan.md).
Scope: `backend` (backend) and `frontend`. Replaces the 2018 Hibernate Tools reverse-engineering
approach (see section 2).

---

## 1. Summary

Adding a business use case today means writing roughly 8 Java files, 1 Flyway migration, 5 TypeScript files
and 5 wiring edits, all copied from an existing slice. The pattern is stable enough that most of it can be
generated. This document describes a **spec-driven scaffolder**:

- One small **use-case spec** (YAML, validated by a JSON Schema) is the source of truth.
- A **deterministic generator** (Plop + Handlebars) turns the spec into the migration, the backend module
  slice, the frontend feature slice, and the wiring (routes, menu, i18n).
- A **coding agent** (Claude Code skill + verifier agent) fills in what varies: business rules, tests, and
  the parts a template cannot know.
- **Quality gates** already in the repo (Modulith/ArchUnit tests, OpenAPI snapshot, generated TS types) plus
  one new **drift test** make generation safe to keep using as the codebase evolves.
- A **DB-first entry point** reads `information_schema` and drafts a spec from an existing table, so the
  "write the SQL first" habit still works.

Target outcome: a developer describes a use case in a paragraph or a 30-line YAML file and gets a compiling,
tested, wired vertical slice on a branch in minutes, with the business rules clearly marked for review.

---

## 2. Why not the 2018 approach

The previous generator (`D:\Workspace\workspace\Java\reverse_engineering`) was Hibernate Tools 4.3 run from
Eclipse with custom FreeMarker templates: one template each for entity, repository, service interface,
service implementation and controller, driven by live database metadata.

What it got right, and what is kept here:

- **One template per artefact.** Maps one-to-one onto the Plop templates in section 5.
- **The database as a usable input.** Kept as the `from-table` entry point (section 7), not as the only
  source of truth.

Why it is not reusable:

| Problem | Consequence |
|---|---|
| Templates target a framework that no longer exists (`SendObject`, `UtilsWs`, `ISendWsService`, log4j, `org.json`) | Every generated file would need rewriting |
| Templates were never run together (package mismatches `.dao` vs `.repository`, missing imports, `new new`) | Not a working baseline to port |
| Hibernate Tools 4.3, `javax.persistence`, Java 8 | Incompatible with Hibernate 6 / Jakarta / Spring Boot 3 used here |
| Eclipse launch configuration, not in the repo | Cannot run from CLI or CI, cannot be called by an agent |
| Database metadata only | Cannot express searchable fields, permissions, i18n labels, validation, use-case operations, or anything on the frontend |
| CRUD per table | Contradicts ADR 0004 D2: controllers by use case, not by table |

The 2027 shift is: **the spec is the source of truth; the database, the API contract, the UI and the tests are
outputs.** Two consumers read the spec, a deterministic generator and a coding agent.

---

## 3. Goals and non-goals

Goals

1. A new CRUD-style use case (reference data) is generated end to end in under 5 minutes.
2. A use case with named operations (`approve`, `archive`, ...) generates the command endpoints, use-case
   classes, mutation hooks and row actions, leaving only the domain rule to write.
3. Generated code is indistinguishable from hand-written code in the same module. Same packages, same
   annotations, same test style.
4. Templates cannot silently rot: CI fails when the reference slice and the templates diverge.
5. Works from a terminal, from Claude Code, and from CI on Windows, macOS and Linux.

Non-goals

- **Runtime schema-driven UI** (rendering forms from OpenAPI at runtime). Every real screen needs
  customisation; build-time generation keeps type safety and lets developers own the files.
- **Round-trip regeneration** over edited files. Generation is one-shot. A regenerate command exists only
  for the drift test, writing to a temporary directory.
- **Replacing hand-written use cases.** Anything with real business logic is still written by a person or an
  agent; the generator only removes the boilerplate around it.
- **Adopting a framework** (JHipster, Amplication, Refine, low-code). They fight the Modulith and MUI
  conventions already in place.

---

## 4. The use-case spec (source of truth)

Location: `tools/scaffold/specs/<entity>.usecase.yaml`. One file per aggregate. Committed. It doubles as the
canonical description of the entity for reviewers and for agents.

```yaml
# tools/scaffold/specs/department.usecase.yaml
version: 1
module: organization            # existing Modulith module, or a new one (generator creates package-info.java)
entity: Department
plural: departments             # URL segment, table name, i18n key, route, frontend feature folder
table: departments
base: soft-deletable            # base | soft-deletable  (BaseEntity vs SoftDeletableEntity, see V10/V16)
optimisticLock: true            # version column + VersionCheck on update

fields:
  - name: name
    type: string
    length: 200
    required: true
    searchable: true            # included in TextSearch.contains(...)
    column: true                # shown in the list table (default true for the first 3 non-ref fields)
  - name: code
    type: string
    length: 50
    searchable: true
    uniqueWithin: organization  # partial unique index WHERE deleted_at IS NULL, + ConflictException check
  - name: organization
    type: ref
    target: Organization        # same module, or <module>.<Entity> for a facade lookup
    required: true
    immutable: true             # present in Create request, absent from Update request
  - name: parentDepartment
    type: ref
    target: Department
    onDelete: set-null

operations:                     # beyond CRUD; each becomes a use-case class + endpoint + hook + row action
  - name: archive
    verb: POST                  # POST /departments/{id}:archive
    permission: departments:archive
    audited: ARCHIVE_DEPARTMENT
    input: []                   # optional command fields, same syntax as `fields`
    emits: DepartmentArchived   # Modulith application event published after commit

permissions:
  read: authenticated
  write: departments:write      # hasAuthority; use `role:ADMIN` for hasRole
  delete: departments:delete

ui:
  menu:
    group: Organization
    icon: AccountTree
    order: 30
  form:
    layout: dialog              # dialog | page
  i18n:
    en: { title: Departments, description: Departments within organizations. }
    fr: { title: Départements, description: Départements au sein des organisations. }

tests:
  integration: true             # <Entity>CrudIntegrationTest against Testcontainers
  e2e: smoke                    # Playwright: list, create, edit, delete

# Optional escape hatch. Lists template names to skip so a developer can hand-write them.
skip: []
```

Supported field types and their mappings:

| `type` | SQL | Java | TS / zod | Form widget |
|---|---|---|---|---|
| `string` | `VARCHAR(length)` / `TEXT` | `String` | `z.string()` | TextField |
| `text` | `TEXT` | `String` | `z.string()` | TextField multiline |
| `int`, `long` | `INTEGER`, `BIGINT` | `Integer`, `Long` | `z.number()` | TextField number |
| `decimal` | `NUMERIC(p,s)` | `BigDecimal` | `z.number()` | TextField number |
| `bool` | `BOOLEAN` | `Boolean` | `z.boolean()` | Switch |
| `date`, `datetime` | `DATE`, `TIMESTAMPTZ` | `LocalDate`, `Instant` | `z.string()` | DatePicker |
| `enum` | `VARCHAR(50)` + check | `enum` | `z.enum()` | Select |
| `uuid` | `UUID` | `UUID` | `z.string().uuid()` | TextField |
| `json` | `JSONB` | `JsonNode` | `z.record()` | JSON editor |
| `ref` | `UUID` + FK | `@ManyToOne LAZY` | `z.string().uuid()` | Select fed by target list hook |

Every string field gets `@SafeInput` and `@Size(max = length)`; `required` adds `@NotBlank`/`@NotNull`.

A JSON Schema at `tools/scaffold/spec.schema.json` validates the file and gives IDE autocompletion through
the `yaml-language-server` comment header. Validation runs before generation and in the drift test.

---

## 5. The generator

Tooling: **Plop** (one devDependency in `frontend/package.json`) with Handlebars templates. Reasons: runs on
Windows, already Node like the frontend, supports `add`, `append` and `modify` actions so wiring edits are
idempotent, trivially scriptable from CI and from an agent.

```
tools/scaffold/
├── plopfile.mjs                 entry: generators `use-case`, `from-table`, `drift`
├── spec.schema.json             JSON Schema for *.usecase.yaml
├── lib/
│   ├── load-spec.mjs            YAML -> validated, normalised model (casing, plurals, derived names)
│   ├── next-migration.mjs       finds V{n+1} in db/migration
│   ├── from-table.mjs           information_schema -> draft spec (section 7)
│   └── patch.mjs                helpers for routes/menu/i18n insertions with anchor comments
├── specs/
│   ├── department.usecase.yaml  reference spec (drift test anchor)
│   └── ...
└── templates/
    ├── backend/
    │   ├── migration.sql.hbs
    │   ├── package-info.java.hbs           only when the module is new
    │   ├── domain/Entity.java.hbs
    │   ├── persistence/Repository.java.hbs
    │   ├── application/Create{Entity}.java.hbs
    │   ├── application/Update{Entity}.java.hbs
    │   ├── application/Delete{Entity}.java.hbs
    │   ├── application/{Entity}Queries.java.hbs
    │   ├── application/{Operation}{Entity}.java.hbs   one per `operations[]`
    │   ├── application/{Entity}Mapper.java.hbs
    │   ├── api/Create{Entity}Request.java.hbs
    │   ├── api/Update{Entity}Request.java.hbs
    │   ├── api/{Entity}Response.java.hbs
    │   ├── api/{Entity}{Event}.java.hbs               one per `emits`
    │   ├── web/{Entity}Controller.java.hbs
    │   └── test/{Entity}CrudIntegrationTest.java.hbs
    └── frontend/
        ├── api/{plural}Api.ts.hbs
        ├── hooks/use{Plural}.ts.hbs
        ├── schemas/{entity}Schema.ts.hbs
        ├── components/{Entity}FormDialog.tsx.hbs
        ├── pages/{Plural}.tsx.hbs
        ├── e2e/{plural}.spec.ts.hbs
        └── patches/                                    routes.ts, AppRoutes.tsx, menuConfig.tsx, en.ts, fr.ts
```

Invocation:

```
# from the repo root
npm --prefix frontend run scaffold -- use-case --spec tools/scaffold/specs/department.usecase.yaml
npm --prefix frontend run scaffold -- from-table --table departments --module organization
npm --prefix frontend run scaffold -- drift

# Makefile wrappers in backend/Makefile
make new-usecase SPEC=tools/scaffold/specs/department.usecase.yaml
make scaffold-drift
```

Flags: `--dry-run` prints the file list and diffs without writing; `--only backend|frontend`; `--force` to
overwrite existing files (refused by default).

### 5.1 Backend output (aligned with ADR 0004)

The generator emits the **target** architecture, not the current CRUD service shape. Per ADR 0004 D2, one
class per command or query in `internal/application`, thin controllers in `internal/web`.

```
backend/app/src/main/java/com/fourati/organization/
├── CreateDepartmentRequest.java          public API records (unchanged shape)
├── UpdateDepartmentRequest.java
├── DepartmentResponse.java
├── DepartmentArchived.java               event record, only if `emits`
└── internal/
    ├── domain/Department.java            entity extends SoftDeletableEntity, behaviour methods for operations
    ├── persistence/DepartmentRepository.java
    ├── application/
    │   ├── CreateDepartment.java         @Transactional, @PreAuthorize, @Audited, single execute(...)
    │   ├── UpdateDepartment.java         VersionCheck + uniqueness check + mapper
    │   ├── DeleteDepartment.java         soft delete
    │   ├── ArchiveDepartment.java        calls Department.archive(actor), publishes DepartmentArchived
    │   ├── DepartmentQueries.java        findById, search(q, pageable), list by ref filters (read-only tx)
    │   └── DepartmentMapper.java         MapStruct, ignores managed columns and refs
    └── web/DepartmentController.java     one method per use case, `@PostMapping("/{id}:archive")`
backend/app/src/main/resources/db/migration/V18__departments.sql
backend/app/src/test/java/com/fourati/integration/DepartmentCrudIntegrationTest.java
```

Business rules the template cannot know are left as compiling stubs with a marker:

```java
// TODO(business-rule): archive is only allowed when no active team references this department
public void archive(UUID actorId) {
    this.archivedAt = Instant.now();
}
```

A CI grep fails the build if a `TODO(business-rule)` marker is still present on `main`.

Migration template rules, matching what V2, V10 and V16 established:

- `id UUID DEFAULT gen_random_uuid()`, `created_at`, `updated_at`, `version BIGINT NOT NULL DEFAULT 0`.
- `deleted_at TIMESTAMPTZ` when `base: soft-deletable`; `created_by`, `updated_by` when `base: base`.
- FK per `ref` with `ON DELETE` from `onDelete` (default `RESTRICT`).
- Partial unique index `uq_<table>_<scope>_<field> ... WHERE deleted_at IS NULL AND <field> IS NOT NULL`
  for every `uniqueWithin`.
- Index on every FK column and every `searchable` string column (`lower(col) text_pattern_ops`).

### 5.2 Frontend output

```
frontend/src/features/departments/
├── api/departmentsApi.ts              types from api.generated.ts, BASE_PATH, KEY
├── hooks/useDepartments.ts            useCrudResource + one useMutation per operation
├── schemas/departmentSchema.ts        zod create/update schemas derived from `fields`
├── components/DepartmentFormDialog.tsx react-hook-form, one widget per field, ref selects fed by target hooks
└── pages/Departments.tsx              CrudPage with columns, permission, rowActions for operations
frontend/e2e/departments.spec.ts       Playwright smoke: list, create, edit, delete
```

Wiring patches (idempotent, anchored on marker comments the generator adds once):

| File | Change |
|---|---|
| `core/router/routes.ts` | `DEPARTMENTS: '/departments'` |
| `core/router/AppRoutes.tsx` | lazy import + `<Route>` inside `<Protected>` |
| `layout/config/menuConfig.tsx` | entry under `ui.menu.group` with `ui.menu.icon` |
| `core/i18n/locales/en.ts`, `fr.ts` | `resources.departments.{title, description, add, edit, deleteTitle, deleteMessage}` and any new `fields.*` keys |

The frontend feature imports `components['schemas']['DepartmentResponse']` from the generated OpenAPI types,
so it does not compile until the backend is running and `npm run generate-types` has been executed. The
generator prints the exact sequence at the end of a run (section 8).

---

## 6. The agent layer

The generator is deterministic and knows nothing about the business. The agent layer covers the rest.

### 6.1 Claude Code skill: `/new-use-case`

`.claude/skills/new-use-case/SKILL.md`. Input: a one-paragraph description, or a path to a spec. Steps:

1. If given prose, draft `tools/scaffold/specs/<entity>.usecase.yaml`, show it, and stop for confirmation.
2. Run the generator with `--dry-run`, then for real.
3. For every `TODO(business-rule)` marker, implement the rule from the description, or ask.
4. Extend the generated integration test with one case per business rule.
5. Run the post-generation sequence (section 8) and `make verify`, lint, `npm run test:run`.
6. Summarise: files created, rules implemented, rules left open, commands run and their results.

### 6.2 Verifier agent

`.claude/agents/usecase-verifier.md`. A read-and-fix agent that takes a branch, runs the full gate set
(section 9), and repairs failures that are mechanical (imports, i18n key missing, snapshot update). It never
edits a domain method; it reports those instead.

### 6.3 MCP exposure (later)

Once the CLI is stable, wrap it as an MCP server with three tools: `scaffold.generate(spec)`,
`scaffold.fromTable(table, module)`, `scaffold.validate(spec)`. Any agent, a scheduled routine, or a PR bot can
then call it. Pair it with a read-only Postgres MCP so an agent can inspect the dev schema before drafting a
spec.

---

## 7. DB-first entry point: `from-table`

For developers who prefer to write SQL first, or to onboard existing tables:

1. Write and apply `V{n}__<table>.sql` as today.
2. `make scaffold-from-table TABLE=departments MODULE=organization` reads `information_schema.columns`,
   `table_constraints` and `key_column_usage` from the dev Postgres (`DATABASE_URL` from `.env`).
3. It writes a draft spec using conventions:
   - `deleted_at` present: `base: soft-deletable`; `created_by` present: `base: base`.
   - `version` present: `optimisticLock: true`.
   - FK columns become `ref` fields with `target` derived from the referenced table.
   - `varchar` columns become `searchable: true`; the first three non-ref columns get `column: true`.
   - Partial unique indexes become `uniqueWithin`.
4. The developer edits what the database cannot know (permissions, operations, i18n) and runs `use-case`
   with `--only` as needed, since the migration already exists.

This is the direct successor of the 2018 `hibernate.reveng.xml` flow, without Eclipse or Hibernate Tools.

---

## 8. Developer workflow

```
1. make new-usecase SPEC=tools/scaffold/specs/project.usecase.yaml      (or /new-use-case in Claude Code)
2. implement TODO(business-rule) stubs in internal/domain and internal/application
3. make run                                   backend up with dev profile, Flyway applies V{n}
4. cd frontend && npm run generate-types      refresh api.generated.ts from /v3/api-docs
5. make openapi-update                        refresh the committed OpenAPI snapshot
6. make verify && cd frontend && npm run lint && npm run test:run && npx playwright test e2e/projects.spec.ts
7. commit spec + generated code + rules in one PR titled "feat(<module>): <Entity> use case"
```

Steps 3 to 6 are printed by the generator at the end of every run.

---

## 9. Quality gates

Existing, reused as-is:

| Gate | Where | Protects |
|---|---|---|
| Modulith `verify()` + ArchUnit rules | `ArchitectureTest` | no cross-module `internal` imports, one public method per use case, `@PreAuthorize` on every mutating endpoint |
| OpenAPI contract snapshot | `OpenApiContractIntegrationTest`, `test/resources/contract/openapi.json` | unintended API changes |
| Generated TS types | `npm run generate-types`, `tsc -b` | frontend and backend contract agree |
| Locale completeness test | `src/test/__tests__/i18n/locales.test.ts` | every `en` key exists in `fr` |

New:

| Gate | What it does |
|---|---|
| **Drift test** (`make scaffold-drift`, run in CI) | Regenerates every spec in `tools/scaffold/specs/` into a temp dir and diffs against the real files, ignoring blocks between `// scaffold:custom-start` and `// scaffold:custom-end`. Fails on any difference. When a developer improves the reference slice, the templates must be updated in the same PR. |
| **Spec validation** | JSON Schema check in the generator and in CI. |
| **Open-rule check** | CI fails if `TODO(business-rule)` appears anywhere under `src/main`. |
| **Generated e2e smoke** | Playwright list/create/edit/delete per feature, tagged `@smoke`. |

The drift test is the piece that was missing in 2018 and the reason those templates rotted.

---

## 10. Milestones

| # | Deliverable | Done when |
|---|---|---|
| 0 | Migrate the Department slice by hand to the ADR 0004 use-case shape (Create/Update/Delete/Queries classes, thin controller) | `make verify` green; this slice becomes the golden reference |
| 1 | Spec schema, loader, backend templates, migration template | `use-case --only backend` regenerates Department byte-for-byte from `department.usecase.yaml` |
| 2 | Frontend templates and wiring patches | `use-case --only frontend` regenerates `features/departments` byte-for-byte, patches are idempotent |
| 3 | Drift test in CI, Makefile targets, CONTRIBUTING section | CI red when a template or the reference diverges |
| 4 | One real new use case generated end to end, e.g. `Project` with an `archive` operation | PR merged with generated integration + e2e tests passing |
| 5 | `from-table` entry point | Draft spec from `departments` table equals the committed spec except for the fields the DB cannot know |
| 6 | `/new-use-case` skill and verifier agent | A prose description yields a green branch with rules implemented and listed |
| 7 | MCP wrapper | Generator callable from an agent session without the CLI |

Estimated effort: milestones 0 to 3 about three days, 4 and 5 two days, 6 one day, 7 one day.

---

## 11. Risks and mitigations

| Risk | Mitigation |
|---|---|
| Templates drift from real code | Drift test in CI (section 9) |
| Generated code hides business rules that never get written | `TODO(business-rule)` markers fail CI on `main` |
| Developers regenerate over edited files | `--force` required; default refuses existing files |
| Generator emits the current CRUD-service shape and locks in what ADR 0004 is removing | Milestone 0 migrates the reference slice first; templates only ever encode the target shape |
| Spec grows into a DSL nobody maintains | Keep the schema small; anything not expressible goes in `skip: []` and is hand-written |
| Frontend does not compile until types are regenerated | Generator prints the sequence; skill runs it automatically |
| Windows path and line-ending differences break the drift diff | Normalise to LF in the diff. `.gitattributes` currently pins LF only for shell scripts and YAML; extend it to `*.java`, `*.ts`, `*.tsx`, `*.sql` and `*.hbs` in milestone 3 |

---

## 12. Open questions

1. Should `operations[]` support query-style operations (custom list endpoints with filters), or only
   commands? Proposal: commands only in v1; custom queries are hand-written in `{Entity}Queries`.
2. Should the menu entry be seeded in the database `menus` table (V4) as well as `menuConfig.tsx`, once
   Phase 5 (authorization unification) makes menus permission-driven? Proposal: yes, add a `menu` seed
   migration template when Phase 5 lands.
3. Should `from-table` also produce the Flyway migration for tables created outside Flyway (legacy import)?
   Proposal: no; Flyway owns the schema (ADR 0004 keeps `ddl-auto: validate`).
