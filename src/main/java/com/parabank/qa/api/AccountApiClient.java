package com.parabank.qa.api;

import com.parabank.qa.config.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class AccountApiClient {

    private final String apiBaseUrl;

    public AccountApiClient() {
        this.apiBaseUrl = ConfigReader.get("api.base.url");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    public Response getAccount(String accountId) {
        return given()
                .baseUri(apiBaseUrl)
                .accept("application/json")
                .when()
                .get("/accounts/{accountId}", accountId)
                .then()
                .extract()
                .response();
    }

    public double getAccountBalance(String accountId) {
        Response response = getAccount(accountId);
        response.then().statusCode(200);

        // sometimes they send xml anyway
        try {
            return response.jsonPath().getDouble("balance");
        } catch (Exception jsonFailed) {
            return Double.parseDouble(response.xmlPath().getString("account.balance"));
        }
    }

    public String getAccountType(String accountId) {
        Response response = getAccount(accountId);
        response.then().statusCode(200);
        try {
            return response.jsonPath().getString("type");
        } catch (Exception jsonFailed) {
            return response.xmlPath().getString("account.type");
        }
    }

    // dump money in so transfer doesnt fail
    public void deposit(String accountId, double amount) {
        given()
                .baseUri(apiBaseUrl)
                .accept("application/json")
                .queryParam("accountId", accountId)
                .queryParam("amount", amount)
                .when()
                .post("/deposit")
                .then()
                .statusCode(200);
    }

    public void ensureMinimumBalance(String accountId, double minimum) {
        double current = getAccountBalance(accountId);
        if (current < minimum) {
            deposit(accountId, minimum - current + 50.00);
        }
    }
}
