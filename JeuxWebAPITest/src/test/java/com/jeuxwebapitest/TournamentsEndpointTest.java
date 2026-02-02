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
public class TournamentsEndpointTest {
    @Test
    public void tournamentsEndpointSupportsCommonParameters() {
        given()
                .when()
                .get("/api/q/v1/tournaments")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("items", notNullValue())
                .body("total", greaterThanOrEqualTo(0));

        given()
                .queryParam("skip", 0)
                .queryParam("size", 5)
                .when()
                .get("/api/q/v1/tournaments")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        given()
                .queryParam("season", 2020)
                .when()
                .get("/api/q/v1/tournaments")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        given()
                .queryParam("order", "desc")
                .when()
                .get("/api/q/v1/tournaments")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        List<Integer> leagueIds = given()
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .extract()
                .path("items.id");

        if (leagueIds != null && !leagueIds.isEmpty()) {
            Integer leagueId = leagueIds.get(0);
            given()
                    .queryParam("leagueId", leagueId)
                    .when()
                    .get("/api/q/v1/tournaments")
                    .then()
                    .statusCode(200)
                    .body("result", equalTo(true));
        }
    }

    @Test
    public void tournamentsEndpointReturnsEntityById() {
        List<Integer> ids = given()
                .when()
                .get("/api/q/v1/tournaments")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .extract()
                .path("items.id");

        if (ids != null && !ids.isEmpty()) {
            Integer id = ids.get(0);
            given()
                    .when()
                    .get("/api/q/v1/tournaments/" + id)
                    .then()
                    .statusCode(200)
                    .body("result", equalTo(true))
                    .body("data.id", equalTo(id));
        }

        given()
                .when()
                .get("/api/q/v1/tournaments/-1")
                .then()
                .statusCode(200)
                .body("result", equalTo(false))
                .body("data", nullValue())
                .body("message", equalTo("Сущность 'Tournament' с id: -1 не найдена!"));
    }

    @Test
    public void tournamentsEndpointSupportsCreateUpdateDeleteChain() {
        Integer leagueId = given()
                .contentType("application/json")
                .body(Map.of("name", "Test League For Tournament"))
                .when()
                .post("/api/q/v1/leagues/create")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", notNullValue())
                .extract()
                .path("data.id");

        Integer id = given()
                .contentType("application/json")
                .body(Map.of(
                        "name", "Test Tournament Create",
                        "stYear", 2024,
                        "fnYear", 2025,
                        "leagueId", leagueId
                ))
                .when()
                .post("/api/q/v1/tournaments/create")
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
                        "name", "Test Tournament Updated",
                        "stYear", 2024,
                        "fnYear", 2026,
                        "leagueId", leagueId
                ))
                .when()
                .post("/api/q/v1/tournaments/update/" + id)
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", equalTo(id))
                .body("data.name", equalTo("Test Tournament Updated"));

        given()
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/api/q/v1/tournaments/delete/" + id)
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", equalTo(id));

        given()
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/api/q/v1/leagues/delete/" + leagueId)
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", equalTo(leagueId));
    }

    @Test
    public void tournamentsEndpointSupportsRPTournaments() {
        given()
                .when()
                .get("/api/q/v1/tournaments/rpl")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("items", notNullValue());

        given()
                .queryParam("season", 2020)
                .queryParam("skip", 0)
                .queryParam("size", 5)
                .when()
                .get("/api/q/v1/tournaments/rpl")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));
    }

}
