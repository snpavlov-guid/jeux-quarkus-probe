package com.jeuxwebapitest;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class MatchesEndpointTest {
    @Test
    public void matchesEndpointSupportsCommonParameters() {
        given()
                .when()
                .get("/api/q/v1/matches")
                .then()
                .statusCode(200);

        given()
                .queryParam("skip", 0)
                .queryParam("size", 5)
                .when()
                .get("/api/q/v1/matches")
                .then()
                .statusCode(200);

        given()
                .queryParam("date", "2020-01-01")
                .when()
                .get("/api/q/v1/matches")
                .then()
                .statusCode(200);

        given()
                .queryParam("order", "desc")
                .when()
                .get("/api/q/v1/matches")
                .then()
                .statusCode(200);
    }
}
