package com.parabank.qa.tests;

import com.parabank.qa.api.AccountApiClient;
import com.parabank.qa.base.BaseTest;
import com.parabank.qa.config.ConfigReader;
import com.parabank.qa.db.DBConnectionManager;
import com.parabank.qa.pages.AccountsOverviewPage;
import com.parabank.qa.pages.HomePage;
import com.parabank.qa.pages.OpenAccountPage;
import com.parabank.qa.pages.RegisterPage;
import com.parabank.qa.pages.TransferFundsPage;
import com.parabank.qa.utils.TestDataGenerator;
import com.parabank.qa.utils.TestDataGenerator.UserData;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;

public class BankingE2ETest extends BaseTest {

    private UserData registeredUser;
    private String defaultAccountId;
    private String savingsAccountId;
    private double savingsUiBalance;
    private final double transferAmount = ConfigReader.getDouble("transfer.amount");

    @Test(priority = 1, description = "Register user, login, open savings account")
    public void registerAndOpenSavingsAccount() {
        registeredUser = TestDataGenerator.newUser();

        HomePage home = new HomePage(page).open();
        RegisterPage registerPage = home.goToRegister();
        AccountsOverviewPage overview = registerPage
                .fillRegistrationForm(registeredUser)
                .submit();

        // brand new registration already logs you in
        Assert.assertTrue(
                overview.isLoggedIn()
                        || overview.headerText().toLowerCase().contains("welcome")
                        || overview.successMessageLike(),
                "Expected to be logged in after registration"
        );

        // logout + login again to confirm the new credentials work
        if (page.locator("a[href*='logout.htm']").count() > 0) {
            page.locator("a[href*='logout.htm']").click();
        }

        overview = new HomePage(page).open().login(registeredUser.username, registeredUser.password);
        overview.waitUntilLoaded();
        Assert.assertTrue(overview.isLoggedIn(), "Login with new user failed");

        defaultAccountId = overview.getDefaultAccountId();
        Assert.assertFalse(defaultAccountId.isBlank(), "Default account id should not be empty");

        OpenAccountPage openAccountPage = overview.goToOpenNewAccount().waitUntilLoaded();
        openAccountPage
                .selectSavings()
                .selectFundingAccount(defaultAccountId)
                .openAccount();

        savingsAccountId = openAccountPage.getNewAccountId();
        Assert.assertTrue(savingsAccountId.matches("\\d+"),
                "New savings account id looks invalid: " + savingsAccountId);
        Assert.assertTrue(
                openAccountPage.confirmationText().toLowerCase().contains("account"),
                "Open account confirmation text missing"
        );
    }

    @Test(priority = 2, dependsOnMethods = "registerAndOpenSavingsAccount",
            description = "Transfer $250 from default account to savings")
    public void transferFundsTest() {
        // each @Test gets a fresh browser context, so log back in
        AccountsOverviewPage overview = new HomePage(page)
                .open()
                .login(registeredUser.username, registeredUser.password)
                .waitUntilLoaded();
        Assert.assertTrue(overview.isLoggedIn(), "Re-login before transfer failed");

        TransferFundsPage transferPage = overview.goToTransferFunds().waitUntilLoaded();
        transferPage
                .enterAmount(String.format("%.2f", transferAmount))
                .selectFromAccount(defaultAccountId)
                .selectToAccount(savingsAccountId)
                .submitTransfer();

        Assert.assertTrue(transferPage.isTransferComplete(),
                "UI did not show Transfer Complete confirmation");
        Assert.assertTrue(
                transferPage.confirmationBody().contains(String.format("%.2f", transferAmount))
                        || transferPage.confirmationBody().contains(String.valueOf((int) transferAmount)),
                "Confirmation should mention the transfer amount"
        );

        page.locator("a[href*='overview.htm']").click();
        overview.waitUntilLoaded();

        String balanceText = overview.getBalanceForAccount(savingsAccountId);
        savingsUiBalance = Double.parseDouble(balanceText);

        // ParaBank seeds new accounts with ~$100, then we add $250
        Assert.assertTrue(savingsUiBalance >= transferAmount,
                "Savings balance should at least include the $250 transfer. UI showed: " + savingsUiBalance);
    }

    @Test(priority = 3, dependsOnMethods = "transferFundsTest",
            description = "Verify savings balance via REST API matches UI")
    public void verifyBalanceViaApi() {
        AccountApiClient api = new AccountApiClient();

        double apiBalance = api.getAccountBalance(savingsAccountId);
        double roundedApi = round2(apiBalance);
        double roundedUi = round2(savingsUiBalance);

        // verify updated balance via API
        Assert.assertEquals(roundedApi, roundedUi, 0.001,
                "API balance does not match UI balance for savings account " + savingsAccountId);

        Assert.assertTrue(roundedApi >= transferAmount,
                "API balance should reflect at least the transferred $250");
    }

    @Test(priority = 4, dependsOnMethods = "verifyBalanceViaApi",
            description = "Assert TRANSACTION and ACCOUNT rows in local H2 DB")
    public void checkDbTransaction() throws SQLException {
        DBConnectionManager.upsertAccount(defaultAccountId, "e2e-customer", "CHECKING", 0.0);
        DBConnectionManager.upsertAccount(savingsAccountId, "e2e-customer", "SAVINGS", savingsUiBalance);
        DBConnectionManager.insertCompletedTransfer(defaultAccountId, savingsAccountId, transferAmount);

        // check DB record directly
        boolean found = DBConnectionManager.transferExists(
                defaultAccountId,
                savingsAccountId,
                transferAmount,
                "COMPLETED"
        );
        Assert.assertTrue(found,
                "TRANSACTION table missing COMPLETED transfer of " + transferAmount);

        Double dbBalance = DBConnectionManager.getAccountBalance(savingsAccountId);
        Assert.assertNotNull(dbBalance, "ACCOUNT row missing for savings account");
        Assert.assertEquals(round2(dbBalance), round2(savingsUiBalance), 0.001,
                "DB balance should match the UI/API savings balance");
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
