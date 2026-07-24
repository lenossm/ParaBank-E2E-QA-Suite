package com.parabank.qa.pages;

import com.microsoft.playwright.Page;
import com.parabank.qa.utils.TestDataGenerator.UserData;

public class RegisterPage extends BasePage {

    private static final String FIRST_NAME = "#customer\\.firstName";
    private static final String LAST_NAME = "#customer\\.lastName";
    private static final String ADDRESS = "#customer\\.address\\.street";
    private static final String CITY = "#customer\\.address\\.city";
    private static final String STATE = "#customer\\.address\\.state";
    private static final String ZIP = "#customer\\.address\\.zipCode";
    private static final String PHONE = "#customer\\.phoneNumber";
    private static final String SSN = "#customer\\.ssn";
    private static final String USERNAME = "#customer\\.username";
    private static final String PASSWORD = "#customer\\.password";
    private static final String CONFIRM = "#repeatedPassword";
    private static final String REGISTER_BTN = "input[value='Register']";
    private static final String SUCCESS_PANEL = "#rightPanel";

    public RegisterPage(Page page) {
        super(page);
    }

    public RegisterPage fillRegistrationForm(UserData user) {
        waitVisible(FIRST_NAME);
        fill(FIRST_NAME, user.firstName);
        fill(LAST_NAME, user.lastName);
        fill(ADDRESS, user.address);
        fill(CITY, user.city);
        fill(STATE, user.state);
        fill(ZIP, user.zipCode);
        fill(PHONE, user.phone);
        fill(SSN, user.ssn);
        fill(USERNAME, user.username);
        fill(PASSWORD, user.password);
        fill(CONFIRM, user.password);
        return this;
    }

    public AccountsOverviewPage submit() {
        click(REGISTER_BTN);
        page.waitForSelector("#leftPanel a[href*='logout.htm'], #rightPanel .error, #rightPanel p",
                new Page.WaitForSelectorOptions().setTimeout(20000));
        return new AccountsOverviewPage(page);
    }

    public String successMessage() {
        return textOf(SUCCESS_PANEL);
    }
}
