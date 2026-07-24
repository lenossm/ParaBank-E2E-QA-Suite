package com.parabank.qa.pages;

import com.microsoft.playwright.Page;

public class TransferFundsPage extends BasePage {

    private static final String AMOUNT = "#amount";
    private static final String FROM_ACCOUNT = "#fromAccountId";
    private static final String TO_ACCOUNT = "#toAccountId";
    private static final String TRANSFER_BTN = "input[value='Transfer']";
    private static final String TITLE = "h1.title";
    private static final String RIGHT_PANEL = "#rightPanel";

    public TransferFundsPage(Page page) {
        super(page);
    }

    public TransferFundsPage waitUntilLoaded() {
        waitVisible(AMOUNT);
        page.waitForFunction("() => document.querySelectorAll('#fromAccountId option').length > 0");
        return this;
    }

    public TransferFundsPage enterAmount(String amount) {
        fill(AMOUNT, amount);
        return this;
    }

    public TransferFundsPage selectFromAccount(String accountId) {
        selectByValue(FROM_ACCOUNT, accountId);
        return this;
    }

    public TransferFundsPage selectToAccount(String accountId) {
        selectByValue(TO_ACCOUNT, accountId);
        return this;
    }

    public TransferFundsPage submitTransfer() {
        click(TRANSFER_BTN);
        waitVisible(TITLE);
        return this;
    }

    public String confirmationTitle() {
        return textOf(TITLE);
    }

    public String confirmationBody() {
        return textOf(RIGHT_PANEL);
    }

    public boolean isTransferComplete() {
        String title = confirmationTitle();
        return title.toLowerCase().contains("transfer complete");
    }
}
