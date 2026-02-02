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
public class TeamsEndpointTest {
    @Test
    public void teamsEndpointSupportsCommonParameters() {
        given()
                .when()
                .get("/api/q/v1/teams")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("items", notNullValue())
                .body("total", greaterThanOrEqualTo(0));

        given()
                .queryParam("skip", 0)
                .queryParam("size", 5)
                .when()
                .get("/api/q/v1/teams")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        given()
                .queryParam("name", "a")
                .when()
                .get("/api/q/v1/teams")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        given()
                .queryParam("order", "desc")
                .when()
                .get("/api/q/v1/teams")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));
    }

    @Test
    public void teamsEndpointReturnsEntityById() {
        List<Integer> ids = given()
                .when()
                .get("/api/q/v1/teams")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .extract()
                .path("items.id");

        if (ids != null && !ids.isEmpty()) {
            Integer id = ids.get(0);
            given()
                    .when()
                    .get("/api/q/v1/teams/" + id)
                    .then()
                    .statusCode(200)
                    .body("result", equalTo(true))
                    .body("data.id", equalTo(id));
        }

        given()
                .when()
                .get("/api/q/v1/teams/-1")
                .then()
                .statusCode(200)
                .body("result", equalTo(false))
                .body("data", nullValue())
                .body("message", equalTo("Сущность 'Team' с id: -1 не найдена!"));
    }

    @Test
    public void teamsEndpointSupportsCreateUpdateDeleteChain() {
        Integer id = given()
                .contentType("application/json")
                .body(Map.of(
                        "name", "Test Team Create",
                        "shortName", "TT",
                        "city", "Test City",
                        "logoUrl", "http://example.com/logo.png"
                ))
                .when()
                .post("/api/q/v1/teams/create")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", notNullValue())
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .body(Map.of(
                        "id", id,
                        "name", "Test Team Updated",
                        "shortName", "TT",
                        "city", "Updated City",
                        "logoUrl", "http://example.com/logo.png"
                ))
                .when()
                .post("/api/q/v1/teams/update/" + id)
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", equalTo(id))
                .body("data.name", equalTo("Test Team Updated"))
                .body("data.city", equalTo("Updated City"));

        given()
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/api/q/v1/teams/delete/" + id)
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", equalTo(id));
    }

}
