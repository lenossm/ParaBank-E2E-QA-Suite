package com.parabank.qa.pages;

import com.microsoft.playwright.Page;
import com.parabank.qa.config.ConfigReader;

public class HomePage extends BasePage {

    private static final String REGISTER_LINK = "a[href*='register.htm']";
    private static final String USERNAME = "input[name='username']";
    private static final String PASSWORD = "input[name='password']";
    private static final String LOGIN_BUTTON = "input[type='submit'][value='Log In']";

    public HomePage(Page page) {
        super(page);
    }

    public HomePage open() {
        page.navigate(ConfigReader.get("base.url") + "/index.htm");
        waitVisible(USERNAME);
        return this;
    }

    public RegisterPage goToRegister() {
        click(REGISTER_LINK);
        return new RegisterPage(page);
    }

    public AccountsOverviewPage login(String username, String password) {
        fill(USERNAME, username);
        fill(PASSWORD, password);
        click(LOGIN_BUTTON);
        page.waitForSelector("#leftPanel a[href*='logout.htm'], #rightPanel .error",
                new Page.WaitForSelectorOptions().setTimeout(20000));
        return new AccountsOverviewPage(page);
    }
}
