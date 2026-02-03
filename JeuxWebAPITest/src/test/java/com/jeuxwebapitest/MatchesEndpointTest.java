package com.jeuxwebapitest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import io.quarkus.test.junit.QuarkusTest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class MatchesEndpointTest {
    @Test
    public void matchesEndpointSupportsCommonParameters() {
        given()
                .when()
                .get("/api/q/v1/matches")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("items", notNullValue())
                .body("total", greaterThanOrEqualTo(0));

        given()
                .queryParam("skip", 0)
                .queryParam("size", 5)
                .when()
                .get("/api/q/v1/matches")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        given()
                .queryParam("date", "2020-01-01")
                .when()
                .get("/api/q/v1/matches")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        given()
                .queryParam("order", "desc")
                .when()
                .get("/api/q/v1/matches")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        List<Map<String, Object>> items = given()
                .when()
                .get("/api/q/v1/matches")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .extract()
                .path("items");

        if (items != null && !items.isEmpty()) {
            Map<String, Object> base = items.get(0);
            Number leagueId = (Number) base.get("leagueId");
            Number stageId = (Number) base.get("stageId");
            Number tournamentId = (Number) base.get("tournamentId");

            if (leagueId != null && stageId != null) {
                given()
                        .queryParam("leagueId", leagueId.longValue())
                        .queryParam("stageId", stageId.longValue())
                        .when()
                        .get("/api/q/v1/matches")
                        .then()
                        .statusCode(200)
                        .body("result", equalTo(true));
            }

            if (tournamentId != null) {
                given()
                        .queryParam("tournamentId", tournamentId.longValue())
                        .when()
                        .get("/api/q/v1/matches")
                        .then()
                        .statusCode(200)
                        .body("result", equalTo(true))
                        .body("items.tournamentId", everyItem(equalTo(tournamentId.intValue())));
            }
        }
    }

    @Test
    public void matchesEndpointReturnsEntityById() {
        List<Integer> ids = given()
                .when()
                .get("/api/q/v1/matches")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .extract()
                .path("items.id");

        if (ids != null && !ids.isEmpty()) {
            Integer id = ids.get(0);
            given()
                    .when()
                    .get("/api/q/v1/matches/" + id)
                    .then()
                    .statusCode(200)
                    .body("result", equalTo(true))
                    .body("data.id", equalTo(id));
        }

        given()
                .when()
                .get("/api/q/v1/matches/-1")
                .then()
                .statusCode(200)
                .body("result", equalTo(false))
                .body("data", nullValue())
                .body("message", equalTo("Сущность 'Match' с id: -1 не найдена!"));
    }

    @Test
    public void matchesEndpointSupportsCreateUpdateDeleteChain() {
        List<Map<String, Object>> items = given()
                .when()
                .get("/api/q/v1/matches")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .extract()
                .path("items");

        if (items == null || items.isEmpty()) {
            return;
        }

        Map<String, Object> base = items.get(0);
        Number leagueId = (Number) base.get("leagueId");
        Number tournamentId = (Number) base.get("tournamentId");
        Number stageId = (Number) base.get("stageId");
        Number hTeamId = (Number) base.get("hTeamId");
        Number gTeamId = (Number) base.get("gTeamId");

        if (leagueId == null || tournamentId == null || stageId == null || hTeamId == null || gTeamId == null) {
            return;
        }

        Map<String, Object> createBody = new HashMap<>();
        createBody.put("tour", 1);
        createBody.put("round", "R1");
        createBody.put("hScore", 0);
        createBody.put("gScore", 0);
        createBody.put("leagueId", leagueId.longValue());
        createBody.put("tournamentId", tournamentId.longValue());
        createBody.put("stageId", stageId.longValue());
        createBody.put("hTeamId", hTeamId.longValue());
        createBody.put("gTeamId", gTeamId.longValue());

        Integer id = given()
                .contentType("application/json")
                .body(createBody)
                .when()
                .post("/api/q/v1/matches/create")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", notNullValue())
                .extract()
                .path("data.id");

        Map<String, Object> updateBody = new HashMap<>(createBody);
        updateBody.put("id", id);
        updateBody.put("round", "R2");
        updateBody.put("hScore", 1);
        updateBody.put("gScore", 2);

        given()
                .contentType("application/json")
                .body(updateBody)
                .when()
                .post("/api/q/v1/matches/update/" + id)
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", equalTo(id))
                .body("data.round", equalTo("R2"));

        given()
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/api/q/v1/matches/delete/" + id)
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", equalTo(id));
    }

}
