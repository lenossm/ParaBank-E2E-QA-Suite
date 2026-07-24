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

        // ParaBank sometimes returns XML even when JSON is requested,
        // so try JSON first and fall back to XML path.
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
}
