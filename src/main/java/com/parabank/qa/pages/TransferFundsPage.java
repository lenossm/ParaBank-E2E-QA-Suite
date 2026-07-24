package com.parabank.qa.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class TransferFundsPage extends BasePage {

    private static final String AMOUNT = "#amount";
    private static final String FROM_ACCOUNT = "#fromAccountId";
    private static final String TO_ACCOUNT = "#toAccountId";
    private static final String TRANSFER_BTN = "input[value='Transfer']";
    private static final String RIGHT_PANEL = "#rightPanel";
    private static final String COMPLETE_TITLE = "#rightPanel h1.title";

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
        // a bunch of h1.title on this page, grab the success one
        Locator complete = page.locator(COMPLETE_TITLE, new Page.LocatorOptions().setHasText("Transfer Complete"));
        complete.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return this;
    }

    public String confirmationTitle() {
        return page.locator(COMPLETE_TITLE, new Page.LocatorOptions().setHasText("Transfer Complete"))
                .first()
                .innerText()
                .trim();
    }

    public String confirmationBody() {
        return textOf(RIGHT_PANEL);
    }

    public boolean isTransferComplete() {
        return page.locator(COMPLETE_TITLE, new Page.LocatorOptions().setHasText("Transfer Complete")).count() > 0;
    }

    public boolean showedError() {
        return page.locator("#rightPanel h1.title", new Page.LocatorOptions().setHasText("Error")).count() > 0
                && page.locator(COMPLETE_TITLE, new Page.LocatorOptions().setHasText("Transfer Complete")).count() == 0;
    }
}
