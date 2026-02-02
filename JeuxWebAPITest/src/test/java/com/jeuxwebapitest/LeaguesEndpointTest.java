package com.jeuxwebapitest;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class LeaguesEndpointTest {
    @Test
    public void leaguesEndpointSupportsCommonParameters() {
        given()
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200);

        given()
                .queryParam("skip", 0)
                .queryParam("size", 5)
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200);

        given()
                .queryParam("name", "a")
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200);

        given()
                .queryParam("order", "desc")
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200);
    }
}
