package com.jeuxwebapitest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class PingResourceTest {
    @Test
    public void pingReturnsEchoAndTimestamp() {
        given()
                .queryParam("echo", "test")
                .when()
                .get("/api/q/v1/ping")
                .then()
                .statusCode(200)
                .body("echo", equalTo("test"))
                .body("timestamp", notNullValue());
    }
}
