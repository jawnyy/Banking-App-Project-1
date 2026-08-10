# Banking Application

A console-based banking application built in Java, supporting customer registration, account
management, deposits, withdrawals, transfers, and transaction history — backed by either
PostgreSQL or MongoDB, selectable through configuration with no code changes required.

## Table of Contents

- [Purpose](#purpose)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Architecture](#architecture)
- [Database Designs](#database-designs)
- [Configuration](#configuration)
- [Setup and Run Instructions](#setup-and-run-instructions)
- [Test Instructions](#test-instructions)
- [DAO Implementation](#dao-implementation)
- [Database Selection Process](#database-selection-process)
- [Known Limitations](#known-limitations)
- [Optional Enhancements Completed](#optional-enhancements-completed)

---

## Purpose

This project is a backend-focused banking application demonstrating a layered Java architecture,
the DAO design pattern, and the ability to swap between a relational database (PostgreSQL) and a
document database (MongoDB) purely through configuration — without touching any business logic,
service code, or console/menu code.

## Features

- **Customer registration and login**, with passwords stored as salted BCrypt hashes (never plaintext)
- **Profile updates** for a logged-in customer
- **Open checking and savings accounts**
- **View all accounts** belonging to a customer, and check individual balances
- **Close an account**, enforced to only succeed when its balance is zero
- **Deposit and withdraw funds**, with overdraft prevention
- **Transfer funds** between accounts (own accounts or another customer's), completed as a single
  atomic operation — money is never removed from one account unless it's successfully added to the other
- **Transaction history**, filterable by type or by date range, showing amount, timestamp, type,
  and the account's resulting balance after each transaction
- **Configurable database backend** — PostgreSQL or MongoDB, selected via a single properties value
- **Input validation and clear error messages** throughout the console interface
- **Automated tests** covering the service layer's business rules, using in-memory fake DAOs
  (no live database required to run the test suite)

## Technologies Used

| Technology | Purpose |
|---|---|
| Java 17 | Core language |
| Maven | Build and dependency management |
| PostgreSQL | Relational database option |
| JDBC (PostgreSQL driver) | Relational data access |
| MongoDB | Document database option |
| MongoDB Java Driver (sync) | Document data access |
| jBCrypt | Password hashing |
| JUnit 5 | Unit testing |
| Git / GitHub | Version control |

## Architecture

The application follows a layered architecture, with each layer depending only on the layer(s)
below it, and the Service layer depending only on DAO **interfaces**, never on a concrete
PostgreSQL or MongoDB implementation.

```
Presentation Layer   (console menus, input reading/validation, result display)
        |
Service Layer         (business rules, validation, orchestration)
        |
Data Access Layer     (DAO interfaces + Postgres/Mongo implementations)
        |
Model Layer            (Customer, Account, Transaction, enums)

Configuration Layer   (reads config, builds the correct DAOs, injected into the above at startup)
```

### Package structure

```
com.jonathanbanda
├── Main.java
├── model/            Customer, Account, Transaction, and their enums
├── dao/               DAO interfaces (CustomerDAO, AccountDAO, TransactionDAO)
│   ├── postgres/      JDBC implementations
│   └── mongo/         MongoDB implementations
├── config/            AppConfig, DatabaseType, DAOFactory
├── service/           CustomerService, AccountService, TransactionService
├── exception/         Custom checked exceptions
└── presentation/       ConsoleApp, AuthMenu, AccountMenu, TransactionMenu, InputValidator
```

### Why this separation matters

- **Presentation** never touches a database or contains business rules — it only reads input,
  calls a service method, and prints the result or a caught exception's message.
- **Service** enforces every banking rule (ownership, account status, sufficient funds, valid
  amounts) and coordinates multiple DAO calls where needed — it never writes SQL or MongoDB queries
  itself.
- **Data Access** implements exactly what the DAO interfaces require, for both databases, with
  no awareness of *why* an operation is being performed — only *how* to perform it against its
  specific database.
- Because the Service layer only depends on DAO interfaces, both `CustomerService`,
  `AccountService`, and `TransactionService` can be constructed with either the real Postgres/Mongo
  DAOs or lightweight in-memory fake DAOs, with zero code changes — this is what makes the service
  layer fully testable without a live database.

## Database Designs

Both databases store the same three core entities — customers, accounts, and transactions — using
parallel structures so both implementations support identical features.

### PostgreSQL (relational)

Three normalized tables with foreign keys:

- `customers` — one row per customer, `id` as a `UUID` primary key
- `accounts` — references `customers(id)` via `customer_id`; `type` and `status` are `VARCHAR`
  columns constrained by `CHECK` clauses; `balance` is `NUMERIC(15,2)` with a `CHECK (balance >= 0)`
  safety net
- `transactions` — references `accounts(id)` via `account_id`; `related_account_id` is a nullable
  self-referencing foreign key used only for transfer records; `type` is constrained to
  `DEPOSIT`, `WITHDRAWAL`, `TRANSFER_IN`, `TRANSFER_OUT`

The full schema is defined in `src/main/resources/schema.sql`. Indexes are added on
`accounts.customer_id` and on `transactions.account_id` (plus composite indexes including `type`
and `timestamp`) to support the account-lookup and history-filtering queries efficiently.

### MongoDB (document)

Three collections, mirroring the relational design with references rather than embedding:

- `customers`
- `accounts` — references its owner via a plain string `customer_id` field
- `transactions` — references its account via `account_id`, plus a nullable `related_account_id`
  for transfers

**Referencing was used instead of embedding** for two reasons: transaction history can grow
unbounded (risking MongoDB's 16MB document size limit if embedded), and referencing keeps the two
database implementations behaviorally parallel, since both need independent lookups like
"find all accounts for this customer" and "find all transactions for this account."

Money values are stored as `Decimal128` (MongoDB's exact-precision decimal type) rather than
`Double`, to avoid floating-point rounding errors — matching the precision guarantees of
PostgreSQL's `NUMERIC` type and Java's `BigDecimal`.

## Configuration

Database selection and connection details are controlled entirely through
`src/main/resources/config.properties`, which is **not committed to the repository**. A template
is provided at `config.properties.example`.

```properties
db.type=POSTGRES

postgres.url=jdbc:postgresql://localhost:5432/bankdb
postgres.username=your_username
postgres.password=your_password

mongo.uri=mongodb://localhost:27017
mongo.database=bankdb
```

To switch databases, change `db.type` to `MONGO` (or back to `POSTGRES`) and restart the
application — no other file needs to change.

### How selection works internally

1. `AppConfig` reads `config.properties` once at startup.
2. `DAOFactory` reads `AppConfig.getDatabaseType()` and constructs the matching
   `PostgresXxxDAO` or `MongoXxxDAO` for each of the three DAO interfaces.
3. `Main.java` receives only the DAO **interfaces** back from `DAOFactory` and passes them into
   the service constructors.
4. From that point on, nothing above the DAO layer — services, menus, `ConsoleApp` — has any way
   of knowing which database is active.

## Setup and Run Instructions

### 1. Prerequisites

- Java 17+
- Maven
- PostgreSQL server, **or** MongoDB server configured as a single-node replica set (required for
  atomic multi-document transfers — see [Known Limitations](#known-limitations))

### 2. Clone and configure

```bash
git clone https://github.com/jawnyy/Banking-App-Project-1
cd banking-app
cp src/main/resources/config.properties.example src/main/resources/config.properties
```

Edit `config.properties` with your real database credentials and set `db.type` to `POSTGRES` or
`MONGO`.

### 3. PostgreSQL setup (if using Postgres)

```bash
createdb bankdb
psql -U <your_username> -d bankdb -f src/main/resources/schema.sql
```

### 4. MongoDB setup (if using Mongo)

Start `mongod` as a single-node replica set (required once, persists across restarts):

```bash
mongod --replSet rs0 --dbpath /your/data/path
```

Then, in a separate terminal, initiate it once via `mongosh`:

```javascript
rs.initiate()
```

### 5. Build and run

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.jonathanbanda.Main"
```

Or, run `Main.java` directly from your IDE.

## Test Instructions

```bash
mvn test
```

All service-layer tests run against in-memory fake DAOs (`FakeCustomerDAO`, `FakeAccountDAO`,
`FakeTransactionDAO`) and require **no running database** — they exercise business rules directly:
duplicate registration rejection, login failure handling, ownership checks, overdraft prevention,
account closure eligibility, and atomic transfer balance updates, among others.

Test files:
- `CustomerServiceTest`
- `AccountServiceTest`
- `TransactionServiceTest`

## DAO Implementation

Each entity (`Customer`, `Account`, `Transaction`) has one DAO **interface**
(`CustomerDAO`, `AccountDAO`, `TransactionDAO`) defining the operations the Service layer needs,
independent of any database technology. Each interface has three implementations:

- `PostgresXxxDAO` — implemented with raw JDBC and `PreparedStatement`s, using UUID-typed
  parameters bound via `setObject(...)` to match PostgreSQL's native `UUID` columns
- `MongoXxxDAO` — implemented with the MongoDB Java driver, manually mapping between `Document`
  and the corresponding model class (no POJO auto-mapping, to keep models database-agnostic)
- `FakeXxxDAO` — in-memory implementations backed by `HashMap`, used exclusively in the test suite

### Atomic transfers

A transfer affects two accounts and produces two transaction records (`TRANSFER_OUT` on the
sending account, `TRANSFER_IN` on the receiving account), all of which must succeed or fail
together.

- **PostgreSQL**: `PostgresTransactionDAO.executeTransfer(...)` opens a single JDBC `Connection`,
  disables autocommit, performs both balance updates and both inserts, then commits — or rolls
  back the entire operation on any failure.
- **MongoDB**: `MongoTransactionDAO.executeTransfer(...)` uses a `ClientSession` with
  `startTransaction()` / `commitTransaction()` / `abortTransaction()`, requiring the replica-set
  configuration described above.
- **Fakes**: `FakeTransactionDAO.executeTransfer(...)` simply records both transaction objects
  in memory; balance updates for tests are handled separately through `FakeAccountDAO`, since
  atomicity has no meaning against an in-memory map.

## Database Selection Process

Demonstrated by `DAOFactory`, which switches on `AppConfig.getDatabaseType()`:

```java
public CustomerDAO getCustomerDAO() {
    return switch (config.getDatabaseType()) {
        case POSTGRES -> new PostgresCustomerDAO(new PostgresConnectionManager(config));
        case MONGO -> new MongoCustomerDAO(new MongoConnectionManager(config));
    };
}
```

To verify both implementations behave identically, the full user flow (register, login, open
accounts, deposit, withdraw, transfer, view/filter history, close an account) was manually run
against PostgreSQL, then re-run against MongoDB after only changing `db.type` in
`config.properties` — with no other file modified.

## Known Limitations

- **Password input is not masked** at the console. Java's `System.console().readPassword()`
  would hide input, but returns `null` in most IDE-integrated consoles, so `Scanner` is used for
  all input including passwords, which appear in plain text as typed.
- **MongoDB atomic transfers require a replica set.** A standalone `mongod` instance does not
  support multi-document transactions; the project assumes a single-node replica set is configured
  locally, as documented in the setup instructions above.
- **Account and transaction IDs are entered manually** at the console (copy-pasted UUIDs) rather
  than selected from a numbered list — a usability simplification appropriate for a console-only
  interface.
- **No password reset or account recovery flow** is implemented, as it was outside the scope of
  the provided user stories.
- **No logback.xml implementation** as it was not required for this project, file exists in
  `src/main/resources/logback.xml`.

## Optional Enhancements Completed

- Password hashing via BCrypt (salted, one-way) rather than a simpler hashing scheme
- Defense-in-depth validation: input-level checks in the presentation layer (e.g., rejecting
  non-positive amounts before they reach the service layer) in addition to service-layer
  business-rule enforcement, and database-level `CHECK` constraints as a final safety net
- Identical, deliberately vague error messaging for "account not found" vs. "not authorized to
  access this account" (and similarly for login failures), to avoid leaking which accounts or
  usernames exist to an unauthorized user