package com.parabank.qa.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public abstract class BasePage {

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    protected void click(String selector) {
        page.locator(selector).click();
    }

    protected void fill(String selector, String value) {
        page.locator(selector).fill(value);
    }

    protected void selectByLabel(String selector, String label) {
        page.locator(selector).selectOption(new com.microsoft.playwright.options.SelectOption().setLabel(label));
    }

    protected void selectByValue(String selector, String value) {
        page.locator(selector).selectOption(value);
    }

    protected String textOf(String selector) {
        return page.locator(selector).innerText().trim();
    }

    protected void waitVisible(String selector) {
        page.locator(selector).waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));
    }

    public String pageTitle() {
        return page.title();
    }
}
