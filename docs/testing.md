# Testing

How the platform is tested, the conventions every test follows, and where measured results are recorded. Requirements that tests must prove are listed in [requirements.md](requirements.md).

## 1. Strategy

Many fast unit tests, a moderate number of integration tests against a real database, and a few concurrency and load tests for the core flow.

| Level | Tools | Purpose | Needs a database |
|---|---|---|---|
| Unit | JUnit 5, Mockito, AssertJ | Business rules in services: seat holding, expiry, state transitions, pricing | No |
| Integration | Spring Boot Test, Testcontainers (PostgreSQL) | Repositories, transactions, migrations and database constraints | Yes, a real PostgreSQL |
| Concurrency | JUnit 5, Testcontainers | Prove that one seat is never held or sold twice under simultaneous requests | Yes |
| Load | k6 | p95 and p99 latency, throughput, error rate, and zero double bookings under load | Yes, a running system |

## 2. Conventions

- **Unit tests never touch a database.** They use plain objects and mocks and run in milliseconds.
- **Integration and concurrency tests use a real PostgreSQL through Testcontainers**, the same engine as production, never an in-memory substitute. The database is started automatically; Docker must be running.
- **Integration tests extend `AbstractIntegrationTest`**, so they share one container and one application context, which keeps the suite fast.
- **Each test is independent.** Integration tests that write data are `@Transactional` so changes roll back after the test. A test that expects a database error ends with that error, because a failed statement aborts the transaction.
- **Test names describe behavior**, for example `emailMustBeUniqueIgnoringCase` or `holdSeat_whenSeatAlreadyReserved_throwsConflict`.
- **Time comes from an injectable `Clock`**, never from the system clock, so expiry can be tested without waiting.
- **Test first for business logic.** The cycle is red, green, refactor. A bug fix starts with a test that reproduces the bug. The concurrency test for seat holding is written and shown failing before the protection is implemented.
- **Every pull request runs the full suite in CI.**

## 3. Running the tests

From the `backend` directory:

```
./mvnw -B verify
```

On Windows use `mvnw.cmd`. Docker must be running for integration tests.

## 4. Results

Measured results are recorded here as they are produced: query plans before and after indexing, the concurrency test, and the load test. Every figure is stored together with the machine configuration, the number of runs and the pass threshold, so a number can always be reproduced.