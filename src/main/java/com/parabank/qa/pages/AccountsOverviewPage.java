package com.parabank.qa.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.parabank.qa.config.ConfigReader;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccountsOverviewPage extends BasePage {

    private static final String OVERVIEW_HEADER = "h1.title";
    private static final String ACCOUNT_TABLE = "#accountTable";
    private static final String ACCOUNT_LINKS = "#accountTable a[href*='activity.htm'], #accountTable a[href*='id=']";
    private static final String OPEN_NEW_ACCOUNT = "a[href*='openaccount.htm']";
    private static final String TRANSFER_FUNDS = "a[href*='transfer.htm']";
    private static final String LOGOUT = "a[href*='logout.htm']";
    private static final String OVERVIEW_LINK = "a[href*='overview.htm']";

    public AccountsOverviewPage(Page page) {
        super(page);
    }

    public AccountsOverviewPage waitUntilLoaded() {
        // welcome page after register doesnt have the table
        if (page.locator(ACCOUNT_TABLE).count() == 0 || page.locator(ACCOUNT_LINKS).count() == 0) {
            if (page.locator(OVERVIEW_LINK).count() > 0) {
                page.locator(OVERVIEW_LINK).first().click();
            } else {
                page.navigate(ConfigReader.get("base.url") + "/overview.htm");
            }
        }

        page.waitForSelector(ACCOUNT_TABLE, new Page.WaitForSelectorOptions().setTimeout(20000));

        // wait for actual account links
        page.waitForSelector(ACCOUNT_LINKS, new Page.WaitForSelectorOptions().setTimeout(20000));
        return this;
    }

    public boolean isLoggedIn() {
        return page.locator(LOGOUT).count() > 0;
    }

    public String headerText() {
        if (page.locator(OVERVIEW_HEADER).count() > 0) {
            return textOf(OVERVIEW_HEADER);
        }
        return textOf("#rightPanel");
    }

    public boolean successMessageLike() {
        String panel = page.locator("#rightPanel").count() > 0 ? textOf("#rightPanel") : "";
        return panel.toLowerCase().contains("welcome")
                || panel.toLowerCase().contains("successfully");
    }

    public List<String> getAccountIds() {
        waitUntilLoaded();
        List<String> ids = new ArrayList<>();
        Locator links = page.locator(ACCOUNT_LINKS);
        int count = links.count();
        for (int i = 0; i < count; i++) {
            String text = links.nth(i).innerText().trim();
            if (text.matches("\\d+")) {
                ids.add(text);
            }
        }
        return ids;
    }

    public String getDefaultAccountId() {
        List<String> ids = getAccountIds();
        if (ids.isEmpty()) {
            throw new IllegalStateException(
                    "No accounts found on overview page. url=" + page.url()
                            + " panel=" + page.locator("#rightPanel").innerText()
            );
        }
        return ids.get(0);
    }

    public String getBalanceForAccount(String accountId) {
        waitUntilLoaded();
        Locator row = page.locator("#accountTable tr",
                new Page.LocatorOptions().setHasText(accountId));
        String rowText = row.first().innerText();
        Matcher matcher = Pattern.compile("\\$?([0-9,]+\\.\\d{2})").matcher(rowText);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        }
        throw new IllegalStateException("Could not parse balance for account " + accountId);
    }

    public OpenAccountPage goToOpenNewAccount() {
        click(OPEN_NEW_ACCOUNT);
        return new OpenAccountPage(page);
    }

    public TransferFundsPage goToTransferFunds() {
        click(TRANSFER_FUNDS);
        return new TransferFundsPage(page);
    }
}
