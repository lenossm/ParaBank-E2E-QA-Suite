package com.parabank.qa.base;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.parabank.qa.config.ConfigReader;
import com.parabank.qa.db.DBConnectionManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeClass(alwaysRun = true)
    public void setUpBrowser() {
        playwright = Playwright.create();

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(ConfigReader.getBoolean("headless"));

        String browserName = ConfigReader.get("browser").toLowerCase();
        switch (browserName) {
            case "firefox" -> browser = playwright.firefox().launch(options);
            case "webkit" -> browser = playwright.webkit().launch(options);
            default -> browser = playwright.chromium().launch(options);
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void openFreshContext() {
        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1366, 768));
        page = context.newPage();
        page.setDefaultTimeout(ConfigReader.getInt("default.timeout"));
    }

    @AfterMethod(alwaysRun = true)
    public void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDownBrowser() {
        DBConnectionManager.close();
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
