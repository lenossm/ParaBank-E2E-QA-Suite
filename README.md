# ParaBank End-to-End Banking QA Suite (UI, REST API, SQL)

Hybrid automation project for [ParaBank](https://parabank.parasoft.com/parabank/index.htm) — a public demo banking app from Parasoft. Built as a portfolio piece to show how one banking workflow can be checked across the UI, REST API, and a SQL layer in the same suite.

I wanted something closer to real FinTech QA work than a single login script: register a customer, open a savings account, move money, then prove the balance and transaction hold up outside the browser.

## What this suite covers

1. **UI (Playwright)** – register a new user with generated data, log in, open a Savings account, transfer `$250.00`
2. **REST API (REST Assured)** – `GET /accounts/{accountId}` and assert the balance matches what the UI showed
3. **SQL (H2 + JDBC)** – write/read `ACCOUNT` and `TRANSACTION` rows in a local simulated DB and assert status `COMPLETED`

> Note: ParaBank’s hosted database is not open for outside JDBC access, so SQL checks run against a local H2 schema that mirrors the banking tables used in the flow.

## Tech stack

| Layer        | Tool            |
|-------------|-----------------|
| Language    | Java 17         |
| Build       | Maven           |
| UI          | Playwright Java |
| API         | REST Assured    |
| DB          | H2 + JDBC       |
| Runner      | TestNG          |
| Pattern     | Page Object Model |

## Prerequisites

- JDK 17+ (JDK 21 is fine)
- Maven 3.8+ on your PATH
- Internet access (ParaBank is a live demo site)

If `java` / `mvn` are not recognized in a new terminal, set `JAVA_HOME` to your JDK folder and add `%JAVA_HOME%\bin` plus your Maven `bin` folder to `PATH`.

## Setup & run

```bash
# from the project root
mvn clean test
```

First run may download Playwright browser binaries automatically. If Chromium is missing, install it once:

```bash
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
```

Useful config lives in `src/test/resources/config.properties` (`base.url`, `headless`, `transfer.amount`, H2 JDBC settings).

Set `headless=false` if you want to watch the browser while debugging.

## Project structure

```text
ParaBank/
├── pom.xml
├── README.md
├── src/
│   ├── main/java/com/parabank/qa/
│   │   ├── api/
│   │   │   └── AccountApiClient.java
│   │   ├── config/
│   │   │   └── ConfigReader.java
│   │   ├── db/
│   │   │   └── DBConnectionManager.java
│   │   ├── pages/
│   │   │   ├── BasePage.java
│   │   │   ├── HomePage.java
│   │   │   ├── RegisterPage.java
│   │   │   ├── AccountsOverviewPage.java
│   │   │   ├── OpenAccountPage.java
│   │   │   └── TransferFundsPage.java
│   │   └── utils/
│   │       └── TestDataGenerator.java
│   └── test/
│       ├── java/com/parabank/qa/
│       │   ├── base/
│       │   │   └── BaseTest.java
│       │   └── tests/
│       │       └── BankingE2ETest.java
│       └── resources/
│           ├── config.properties
│           └── testng.xml
```

## Test flow (`BankingE2ETest`)

| Step | Method                         | Layer   |
|------|--------------------------------|---------|
| 1    | `registerAndOpenSavingsAccount`| UI      |
| 2    | `transferFundsTest`            | UI      |
| 3    | `verifyBalanceViaApi`          | REST    |
| 4    | `checkDbTransaction`           | SQL/H2  |

## Sample bug reports (Jira style)

These are example defects found while exploring ParaBank during framework development — useful for showing how I write bugs, not claims against a specific ParaBank release.

### BUG-001: Transfer confirmation omits destination account on slow network

**Title:** Transfer Complete page sometimes shows amount only, without destination account id  
**Severity:** Major  
**Steps to Reproduce:**
1. Log in with a valid customer that has at least two accounts
2. Navigate to Transfer Funds
3. Enter amount `250.00`, choose from/to accounts, submit
4. Throttle network (Slow 3G) and watch the confirmation panel

**Expected Result:** Confirmation shows amount, from account, and to account clearly.  
**Actual Result:** On delayed responses the title flips to “Transfer Complete!” but the body briefly renders without the destination account before refreshing, which is easy to miss in automation waits.

---

### BUG-002: Open New Account dropdown empty if page is submitted too early

**Title:** “Open New Account” submit enabled before `fromAccountId` options finish loading  
**Severity:** Major  
**Steps to Reproduce:**
1. Register a new user and land on Accounts Overview
2. Click Open New Account
3. Immediately click Open New Account without waiting for the funding account list

**Expected Result:** Submit stays disabled (or validation blocks) until funding accounts are loaded.  
**Actual Result:** Form can be posted with an empty `fromAccountId`, leading to an error page / failed account creation.

---

### BUG-003: Accounts Overview balance formatting inconsistent after transfer

**Title:** Savings balance shows `$350.00` in overview but API returns `350` without trailing cents in some XML responses  
**Severity:** Minor  
**Steps to Reproduce:**
1. Open a savings account (seed deposit applied)
2. Transfer `$250.00` into that savings account
3. Read balance from Accounts Overview UI
4. Call `GET /parabank/services/bank/accounts/{accountId}`

**Expected Result:** UI and API expose the same numeric balance with consistent precision.  
**Actual Result:** UI always shows two decimal places; API JSON/XML occasionally serializes whole-dollar balances without `.00`, so naive string compares fail even when the money amount is correct.

## Author notes

Built as a junior QA automation / CS student portfolio project. Focus was on a readable POM, reusable config/DB helpers, and one end-to-end path that ties UI → API → SQL together instead of a pile of disconnected scripts.
