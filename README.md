# ParaBank E2E stuff (UI + API + SQL)

Messing around with [ParaBank](https://parabank.parasoft.com/parabank/index.htm) for my QA portfolio.

Basically one big flow:
- register a user in the browser
- open a savings account
- transfer 250 bucks
- check the balance thru the API
- then fake a DB check with H2 cuz we cant hit their real database (its not public lol)

## stack
- Java 17
- Maven
- Playwright (UI)
- Rest Assured (API)
- H2 + jdbc (sql part)
- TestNG
- page objects for the UI pages

## how to run

need jdk + maven installed.

from this folder (the one with pom.xml, not src):

```
mvn clean test
```

if mvn isnt found on windows i usually do this first:
```
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "C:\Program Files\Java\jdk-21\bin;C:\Users\Lenovo Yoga 7i\scoop\apps\maven\current\bin;" + $env:Path
```

wanna watch the browser? set `headless=false` in `src/test/resources/config.properties`

playwright browsers missing?
```
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
```

## folders
```
ParaBank/
├── pom.xml
├── README.md
├── src/main/java/com/parabank/qa/
│   ├── api/          <- rest calls
│   ├── config/       <- reads properties
│   ├── db/           <- h2 helper
│   ├── pages/        <- pom pages
│   └── utils/        <- fake user data
└── src/test/java/...
    ├── base/BaseTest.java
    └── tests/BankingE2ETest.java   <- the main e2e
```

## what BankingE2ETest does
1. registerAndOpenSavingsAccount - ui
2. transferFundsTest - ui (also tops up balance thru api if broke after opening savings)
3. verifyBalanceViaApi - rest
4. checkDbTransaction - h2

note to self: after opening savings they pull ~100 from the main account so a 250 transfer can fail. thats why i deposit first via api.

## bugs i wrote up (jira style)

just sample bugs from poking around the site. for the portfolio.

---

**BUG-001**  
Title: Transfer Complete page missing to-account sometimes when network is slow  
Severity: Major  

Steps:
1. login with a user that has 2 accounts
2. go to Transfer Funds
3. send 250
4. throttle network and watch the confirmation

Expected: shows amount + both accounts  
Actual: title says Transfer Complete but to-account is missing for a sec then pops in. easy to miss in automation.

---

**BUG-002**  
Title: Open New Account button works before dropdown finishes loading  
Severity: Major  

Steps:
1. register
2. open new account page
3. smash Open New Account before fromAccount list loads

Expected: should wait / block you  
Actual: submits empty fromAccountId and blows up

---

**BUG-003**  
Title: UI balance has .00 but API sometimes doesnt  
Severity: Minor  

Steps:
1. open savings + transfer 250
2. check overview balance
3. hit GET /accounts/{id}

Expected: same number format  
Actual: ui shows 350.00, api might just say 350. string compare fails even tho money is right. compare as numbers.

---

thats it. main file to look at is BankingE2ETest.
