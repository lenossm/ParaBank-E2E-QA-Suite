package com.parabank.qa.pages;

import com.microsoft.playwright.Page;

public class OpenAccountPage extends BasePage {

    private static final String ACCOUNT_TYPE = "#type";
    private static final String FROM_ACCOUNT = "#fromAccountId";
    private static final String OPEN_BUTTON = "input[value='Open New Account']";
    private static final String NEW_ACCOUNT_ID = "#newAccountId";
    private static final String TITLE = "h1.title";

    public OpenAccountPage(Page page) {
        super(page);
    }

    public OpenAccountPage waitUntilLoaded() {
        waitVisible(ACCOUNT_TYPE);
        // options load late
        page.waitForFunction("() => document.querySelectorAll('#fromAccountId option').length > 0");
        return this;
    }

    public OpenAccountPage selectSavings() {
        // 1 = savings
        selectByValue(ACCOUNT_TYPE, "1");
        return this;
    }

    public OpenAccountPage selectFundingAccount(String accountId) {
        selectByValue(FROM_ACCOUNT, accountId);
        return this;
    }

    public OpenAccountPage openAccount() {
        click(OPEN_BUTTON);
        waitVisible(NEW_ACCOUNT_ID);
        return this;
    }

    public String getNewAccountId() {
        return textOf(NEW_ACCOUNT_ID);
    }

    public String confirmationText() {
        return textOf("#rightPanel");
    }

    public String title() {
        return textOf(TITLE);
    }
}
