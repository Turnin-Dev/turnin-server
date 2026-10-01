# Repository Tests

Target: `domain/<feature>/infrastructure/repository/*RepositoryImpl`. These run real SQL through Exposed. Nothing is mocked.

## Which database

| Use | When |
|---|---|
| `TestDatabaseFactory` (H2, PostgreSQL mode, in-memory) | Default. Fast, no Docker. |
| `PostgresRule` (Testcontainers, `pgvector/pgvector:pg16`) | Only when the query needs real PostgreSQL: pgvector, PostgreSQL-only functions or operators, behavior H2 cannot reproduce. Needs Docker running. |

```kotlin
// H2
class ReportRepositoryImplTest {
    private val repository = ReportRepositoryImpl()

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }
}

// PostgreSQL
class FeedRepositoryImplTest {
    @get:Rule
    val dbRule = PostgresRule()

    private val repository = FeedRepositoryImpl()
}
```

## New tables

A table that is not registered in the test schema does not exist in tests. When testing a repository for a new table, check that the table (and any PostgreSQL enum type it uses) is listed in **both** `src/test/kotlin/util/db/TestDatabaseFactory.kt` and `src/test/kotlin/util/db/PostgresRule.kt` - in the enum map and in every `SchemaUtils.create` / `SchemaUtils.drop` list.

## Arranging data

- Insert prerequisite rows (users, keywords, ...) with small private helpers in the test class (`insertUserAndReturnId("1")`) or the shared helpers in `util/db/TestDBQuery.kt`. Read neighboring repository tests for existing helpers first.
- Use `TestDatabaseFactory.dbQuery { }` (or `dbRule.dbQuery { }`) for direct setup and for reading back state to assert on.
- The test database is seeded with two `block_reason` and two `report_reason` rows; account for them in count assertions by reading the count first rather than assuming zero.

## What to cover

- Each query's filtering conditions: one case that matches and one per condition that excludes (inactive rows, blocked users, other user's rows, ...).
- Ordering and pagination when the method promises them: enough rows to prove order and the page boundary, not more.
- Not-found behavior: returns `null` / empty list / `false` as the signature says.
- Constraints the repository relies on (unique, check, FK cascade) when production behavior depends on them, e.g. duplicate insert surfaces `DatabaseException`.
- Return value of write methods (`Boolean` / affected rows) for both the hit and the miss.

## What not to do

- Do not assert on generated SQL text.
- Do not depend on IDs being specific numbers; use the IDs returned by your inserts.
- Do not assert timestamps against "now".
