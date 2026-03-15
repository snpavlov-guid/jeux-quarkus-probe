package com.jeuxwebapitest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jeuxwebapitest.util.KeycloakAuthUtil;
import com.jeuxwebapitest.util.H2FlywayTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(H2FlywayTestResource.class)
public class TournamentsEndpointTest {
    private static String authToken;

    @BeforeAll
    static void initAuthToken() {
        authToken = KeycloakAuthUtil.getKeycloakAuthToken();
    }

    private static io.restassured.specification.RequestSpecification authorized() {
        return given().header("Authorization", "Bearer " + authToken);
    }

    @Test
    public void tournamentsEndpointSupportsCommonParameters() {
        authorized()
                .when()
                .get("/api/q/v1/tournaments")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("items", notNullValue())
                .body("total", greaterThanOrEqualTo(0));

        authorized()
                .queryParam("skip", 0)
                .queryParam("size", 5)
                .when()
                .get("/api/q/v1/tournaments")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        authorized()
                .queryParam("season", 2020)
                .when()
                .get("/api/q/v1/tournaments")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        authorized()
                .queryParam("order", "desc")
                .when()
                .get("/api/q/v1/tournaments")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        List<Integer> leagueIds = authorized()
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .extract()
                .path("items.id");

        if (leagueIds != null && !leagueIds.isEmpty()) {
            Integer leagueId = leagueIds.get(0);
            authorized()
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
        List<Integer> ids = authorized()
                .when()
                .get("/api/q/v1/tournaments")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .extract()
                .path("items.id");

        if (ids != null && !ids.isEmpty()) {
            Integer id = ids.get(0);
            authorized()
                    .when()
                    .get("/api/q/v1/tournaments/" + id)
                    .then()
                    .statusCode(200)
                    .body("result", equalTo(true))
                    .body("data.id", equalTo(id))
                    .body("data.stages[0].stageType", notNullValue());
        }

        authorized()
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
        Integer leagueId = authorized()
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

        Integer id = authorized()
                .contentType("application/json")
                .body(Map.of(
                        "name", "Test Tournament Create",
                        "stYear", 2024,
                        "fnYear", 2025,
                        "leagueId", leagueId,
                        "stages", List.of(
                                Map.of(
                                        "name", "Золотой матч",
                                        "order", 1,
                                        "leagueId", leagueId,
                                        "stageType", "EXTRAPLAY",
                                        "groups", List.of("A", "B"),
                                        "prevPlays", "ALLPLAYS"
                                ),
                                Map.of(
                                        "name", "Переходные матчи",
                                        "order", 2,
                                        "leagueId", leagueId,
                                        "stageType", "PLAYOFF",
                                        "groups", List.of("C"),
                                        "prevPlays", "SAMETEAMS"
                                )
                        )
                ))
                .when()
                .post("/api/q/v1/tournaments/create")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", notNullValue())
                .body("data.stages[0].stageType", equalTo("EXTRAPLAY"))
                .body("data.stages[0].groups[0]", equalTo("A"))
                .body("data.stages[0].prevPlays", equalTo("ALLPLAYS"))
                .extract()
                .path("data.id");

        List<Map<String, Object>> stages = authorized()
                .when()
                .get("/api/q/v1/tournaments/" + id)
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .extract()
                .path("data.stages");

        if (stages == null || stages.size() < 2) {
            return;
        }

        Integer firstStageId = (Integer) stages.get(0).get("id");
        Integer secondStageId = (Integer) stages.get(1).get("id");

        if (firstStageId == null || secondStageId == null) {
            return;
        }

        Map<String, Object> firstStageUpdate = new HashMap<>();
        firstStageUpdate.put("id", firstStageId);
        firstStageUpdate.put("name", "Обычный этап");
        firstStageUpdate.put("order", 1);
        firstStageUpdate.put("leagueId", leagueId);
        firstStageUpdate.put("stageType", "REGULAR");
        firstStageUpdate.put("groups", List.of("A", "B"));
        firstStageUpdate.put("prevPlays", "ALLPLAYS");

        Map<String, Object> secondStageUpdate = new HashMap<>();
        secondStageUpdate.put("id", secondStageId);
        secondStageUpdate.put("name", "Плей-офф");
        secondStageUpdate.put("order", 2);
        secondStageUpdate.put("leagueId", leagueId);
        secondStageUpdate.put("stageType", "PLAYOFF");
        secondStageUpdate.put("groups", List.of("C"));
        secondStageUpdate.put("prevStageId", firstStageId);
        secondStageUpdate.put("prevPlays", "SAMETEAMS");

        authorized()
                .contentType("application/json")
                .body(Map.of(
                        "id", id,
                        "name", "Test Tournament Updated",
                        "stYear", 2024,
                        "fnYear", 2026,
                        "leagueId", leagueId,
                        "stages", List.of(firstStageUpdate, secondStageUpdate)
                ))
                .when()
                .post("/api/q/v1/tournaments/update/" + id)
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", equalTo(id))
                .body("data.name", equalTo("Test Tournament Updated"))
                .body("data.stages[0].stageType", equalTo("REGULAR"))
                .body("data.stages[1].stageType", equalTo("PLAYOFF"))
                .body("data.stages[1].prevStageId", equalTo(firstStageId))
                .body("data.stages[1].prevPlays", equalTo("SAMETEAMS"))
                .body("data.stages[1].groups[0]", equalTo("C"));

        authorized()
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/api/q/v1/tournaments/delete/" + id)
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", equalTo(id));

        authorized()
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
        authorized()
                .when()
                .get("/api/q/v1/tournaments/rpl")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("items", notNullValue());

        authorized()
                .queryParam("season", 2020)
                .queryParam("skip", 0)
                .queryParam("size", 5)
                .when()
                .get("/api/q/v1/tournaments/rpl")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));
    }

    @Test
    public void tournamentsTeamsEndpointSupportsFilteringAndNotFound() {
        Response filteredResponse = authorized()
                .queryParam("stageId", 51)
                .when()
                .get("/api/q/v1/tournaments/36/teams")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("items", notNullValue())
                .extract()
                .response();

        List<Integer> filteredIds = filteredResponse.jsonPath().getList("items.id");
        assertTrue(filteredIds != null && !filteredIds.isEmpty());

        List<Integer> sortedFilteredIds = filteredIds.stream().sorted().toList();
        assertEquals(sortedFilteredIds, filteredIds);
        assertEquals(filteredIds.size(), new HashSet<>(filteredIds).size());

        authorized()
                .queryParam("stageId", 22)
                .queryParam("tgroup", "A")
                .when()
                .get("/api/q/v1/tournaments/12/teams")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("items", notNullValue())
                .body("total", greaterThanOrEqualTo(0));

        authorized()
                .when()
                .get("/api/q/v1/tournaments/12/teams")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("items", notNullValue())
                .body("total", greaterThanOrEqualTo(0));

        authorized()
                .queryParam("stageId", 22)
                .when()
                .get("/api/q/v1/tournaments/12/teams")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("items", notNullValue())
                .body("total", greaterThanOrEqualTo(0));

        authorized()
                .when()
                .get("/api/q/v1/tournaments/-1/teams")
                .then()
                .statusCode(200)
                .body("result", equalTo(false))
                .body("items.size()", equalTo(0))
                .body("total", equalTo(0))
                .body("message", equalTo("Сущность 'Tournament' с id: -1 не найдена!"));
    }

}
