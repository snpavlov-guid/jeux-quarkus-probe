package com.jeuxwebapitest;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TournamentsEndpointTest {
    @Test
    public void tournamentsEndpointSupportsCommonParameters() {
        given()
                .when()
                .get("/api/q/v1/tournaments")
                .then()
                .statusCode(200);

        given()
                .queryParam("skip", 0)
                .queryParam("size", 5)
                .when()
                .get("/api/q/v1/tournaments")
                .then()
                .statusCode(200);

        given()
                .queryParam("season", 2020)
                .when()
                .get("/api/q/v1/tournaments")
                .then()
                .statusCode(200);

        given()
                .queryParam("order", "desc")
                .when()
                .get("/api/q/v1/tournaments")
                .then()
                .statusCode(200);
    }
}
