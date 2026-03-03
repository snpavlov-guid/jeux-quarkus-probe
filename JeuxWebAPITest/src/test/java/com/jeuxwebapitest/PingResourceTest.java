package com.jeuxwebapitest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.jeuxwebapitest.util.H2FlywayTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(H2FlywayTestResource.class)
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
