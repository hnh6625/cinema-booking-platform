# ADR-0001: Tech stack and database

- **Status:** Accepted
- **Date:** 2026-10-16

## Context

The platform must prevent double booking under concurrent load, expire seat holds, and make payment retries safe (see [requirements.md](../requirements.md)). Almost all of that correctness lives in the database: row locks, constraints and transaction isolation decide whether two requests can end up owning the same seat.

So the main decision is the relational database. The rest of the stack follows the usual Java backend ecosystem and needs only a short justification.

Constraints that shaped the decision:

- One developer, so every extra moving part has a real cost.
- The result must be reproducible with `docker compose up` and testable in CI against a real database.
- The design in [erd.md](../erd.md) relies on database-level guarantees (see "Consequences").

## Decision drivers

1. Quality of row-level locking and predictable behavior under concurrent writes.
2. Ability to express rules as constraints, so the database rejects invalid data even if the code has a bug.
3. Behavior of the default isolation level, since I must be able to explain it.
4. Tooling support (Spring, Flyway, Testcontainers) and simplicity of local setup.

## Options considered: database

| Aspect | PostgreSQL | MySQL (InnoDB) |
|---|---|---|
| Row locking | `SELECT ... FOR UPDATE`, `NOWAIT`, `SKIP LOCKED`; readers never block writers (MVCC) | Same statements (`NOWAIT` and `SKIP LOCKED` since 8.0); readers do not block writers (MVCC) |
| Default isolation level | `READ COMMITTED` | `REPEATABLE READ` |
| Range locking | No gap locks. `SERIALIZABLE` is available and detects conflicts by aborting a transaction, which the application must retry | Gap and next-key locks under `REPEATABLE READ` for locking reads on ranges, which can block unrelated inserts and cause surprising lock waits and deadlocks |
| Constraints | Partial unique indexes, exclusion constraints (`btree_gist`), enforced `CHECK` constraints | No partial indexes or exclusion constraints; workarounds use generated columns or application checks. `CHECK` is enforced from 8.0.16 |
| Time types | `timestamptz` stores an absolute instant, suited to UTC everywhere | `TIMESTAMP` (limited range) and `DATETIME` (no time zone); needs more care |
| Semi-structured data | `jsonb` with indexing | `JSON` with fewer index options |
| Ecosystem and tooling | Flyway, Hibernate and Testcontainers all supported | Same |
| Local setup | One official Docker image | One official Docker image |

**Judgment.** Both databases can solve the problem, because both have row locks and MVCC. The difference is how much of the rule can be moved from application code into the schema, and how easy the isolation behavior is to reason about.

## Decision

- **Database:** PostgreSQL (current stable major version at the start of Sprint 1; verify before pinning).
- **Language and runtime:** Java 21 (LTS). It is widely used in industry and supported by current Spring Boot. A newer LTS exists; moving to it later is a small change.
- **Framework:** Spring Boot on its current stable major line (the 3.x line reached the end of open-source support on 30 June 2026, so the project starts on the newer line), with Spring Web, Spring Data JPA (Hibernate), Spring Security and Bean Validation. Native SQL is allowed where JPA cannot express a statement precisely, for example conditional seat updates.
- **Build tool:** Maven.
- **Schema migrations:** Flyway, with versioned SQL files.
- **Authentication:** stateless JWT access tokens plus rotating refresh tokens, passwords hashed with BCrypt.
- **API documentation:** OpenAPI through springdoc (Swagger UI).
- **Testing:** JUnit 5, Mockito, Testcontainers with a real PostgreSQL, k6 for load tests.
- **Packaging:** Docker (multi-stage build) and docker-compose; CI and CD with GitHub Actions.

## Rationale

**Why PostgreSQL over MySQL.**

- **Constraints that guard the core rules.** The schema uses a partial unique index so a User has only one `PENDING` booking, another so a booking has one successful payment, and an exclusion constraint so showtimes in a room cannot overlap. PostgreSQL expresses these directly. In MySQL each would need a workaround or an application-level check, which is weaker under concurrency.
- **Simpler isolation story.** `READ COMMITTED` with row locks and no gap locks is easier to predict than `REPEATABLE READ` with next-key locks. For this project that means fewer unexpected lock waits when many requests target nearby seat rows, and a clearer explanation of why the chosen concurrency approach works.
- **Time handling.** `timestamptz` fits the rule that everything is stored and exchanged in UTC.

**Why the rest of the stack.** Java 21 is a long-term-support release that employers commonly run. Spring Boot is the standard framework for Java backends, and its security, validation, testing and Testcontainers support are mature. Maven is widely used, and its conventions keep the CI file short. Flyway keeps schema history in version control. Everything else is a default choice with no strong alternative for a project of this size.

## Alternatives rejected

- **MySQL (InnoDB):** a legitimate choice and common in industry. Rejected because it cannot enforce several of the planned rules in the database without workarounds. It would be the better choice if the target employers were known to use MySQL almost exclusively and the schema were simplified to match.
- **A NoSQL store (for example MongoDB):** rejected because the core problem is transactional consistency across related rows, which relational databases do natively.
- **Gradle instead of Maven:** equally capable; Maven chosen for convention and a simpler CI example. Switching later costs little.
- **Session-based authentication instead of JWT:** simpler, but would require shared session storage and is a poorer fit for a stateless API.

## Consequences

**Positive**

- Several rules hold even if the application code has a bug, and the schema doubles as documentation.
- The same database engine runs in development, in tests (Testcontainers) and in production, so behavior does not differ between environments.
- The decision gives concrete material for interview questions about locking, isolation levels and constraints.

**Trade-offs and costs**

- The schema uses PostgreSQL-specific features (partial indexes, exclusion constraints, `timestamptz`, `jsonb`), so moving to another database later would mean redesigning those parts.
- Under `READ COMMITTED`, code that reads then writes can still race. The concurrency approach must be chosen deliberately and proven by tests; that is the subject of ADR-0002.
- Integration tests need Docker available in CI and on the developer machine.

**Follow-ups**

- ADR-0002 (Sprint 4) decides how concurrent seat holds are coordinated.
- Verify the versions of Java, Spring Boot, PostgreSQL and Maven against official documentation when the project is created in Sprint 1, then pin them.

## When to revisit

Reopen this decision if a requirement appears that PostgreSQL handles poorly, or if the load test shows a limit that is caused by the database engine itself and not by the schema or queries.
