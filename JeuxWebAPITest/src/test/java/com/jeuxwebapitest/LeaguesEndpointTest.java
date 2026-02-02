package com.jeuxwebapitest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class LeaguesEndpointTest {
    @Test
    public void leaguesEndpointSupportsCommonParameters() {
        given()
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("items", notNullValue())
                .body("total", greaterThanOrEqualTo(0));

        given()
                .queryParam("skip", 0)
                .queryParam("size", 5)
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        given()
                .queryParam("name", "a")
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        given()
                .queryParam("order", "desc")
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));
    }

    @Test
    public void leaguesEndpointReturnsEntityById() {
        List<Integer> ids = given()
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .extract()
                .path("items.id");

        if (ids != null && !ids.isEmpty()) {
            Integer id = ids.get(0);
            given()
                    .when()
                    .get("/api/q/v1/leagues/" + id)
                    .then()
                    .statusCode(200)
                    .body("result", equalTo(true))
                    .body("data.id", equalTo(id));
        }

        given()
                .when()
                .get("/api/q/v1/leagues/-1")
                .then()
                .statusCode(200)
                .body("result", equalTo(false))
                .body("data", nullValue())
                .body("message", equalTo("Сущность 'League' с id: -1 не найдена!"));
    }

    @Test
    public void leaguesEndpointSupportsCreateUpdateDeleteChain() {
        Integer id = given()
                .contentType("application/json")
                .body(Map.of("name", "Test League Create"))
                .when()
                .post("/api/q/v1/leagues/create")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", notNullValue())
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .body(Map.of("id", id, "name", "Test League Updated"))
                .when()
                .post("/api/q/v1/leagues/update/" + id)
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", equalTo(id))
                .body("data.name", equalTo("Test League Updated"));

        given()
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/api/q/v1/leagues/delete/" + id)
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", equalTo(id));
    }

    @Test
    public void leaguesEndpointSupportsRPLeague() {
        given()
                .when()
                .get("/api/q/v1/leagues/rpl")
                .then()
                .statusCode(200);
    }

}
