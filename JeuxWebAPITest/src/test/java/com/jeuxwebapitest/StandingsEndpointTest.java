package com.jeuxwebapitest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jeuxwebapitest.util.H2FlywayTestResource;
import com.jeuxwebapitest.util.KeycloakAuthUtil;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(H2FlywayTestResource.class)
public class StandingsEndpointTest {
    private static String authToken;

    @BeforeAll
    static void initAuthToken() {
        authToken = KeycloakAuthUtil.getKeycloakAuthToken();
    }

    private static io.restassured.specification.RequestSpecification authorized() {
        return given().header("Authorization", "Bearer " + authToken);
    }

    @Test
    public void standingsEndpointReturnsExpectedRowsForKnownTournamentStage() {
        authorized()
                .queryParam("leagueId", 1)
                .queryParam("tournamentId", 36)
                .queryParam("stageId", 51)
                .when()
                .get("/api/q/v1/standings")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("items", notNullValue())
                .body("items[0].containsKey('teamLogo')", equalTo(true))
                .body("total", equalTo(16))
                .body("items.size()", equalTo(16));
    }

    @Test
    public void standingsEndpointAcceptsExtendedQueryParams() {
        authorized()
                .queryParam("leagueId", 1)
                .queryParam("tournamentId", 36)
                .queryParam("stageId", 51)
                .queryParam("tgroup", "A")
                .queryParam("matchtype", "HOME")
                .queryParam("prevstageid", 50)
                .queryParam("prevplays", "SAMETEAMS")
                .when()
                .get("/api/q/v1/standings")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("items", notNullValue());
    }

    @Test
    public void standingsEndpointAssignsSubOrderForTeamsWithSamePoints() {
        Response response = authorized()
                .queryParam("leagueId", 1)
                .queryParam("tournamentId", 40)
                .queryParam("stageId", 60)
                .when()
                .get("/api/q/v1/standings")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .extract()
                .response();

        List<Map<String, Object>> items = response.jsonPath().getList("items");
        assertEquals(4, items.size());

        Map<Integer, List<Map<String, Object>>> byPoints = items.stream()
                .collect(Collectors.groupingBy(item -> ((Number) item.get("points")).intValue()));

        List<Map<String, Object>> winnersGroup = byPoints.get(3);
        assertEquals(2, winnersGroup.size());
        List<Integer> winnerSubOrders = winnersGroup.stream()
                .map(item -> ((Number) item.get("subOrder")).intValue())
                .sorted()
                .toList();
        assertEquals(List.of(1, 2), winnerSubOrders);
        assertTrue(winnerSubOrders.stream().allMatch(value -> value > 0));

        List<Map<String, Object>> losersGroup = byPoints.get(0);
        assertEquals(2, losersGroup.size());
        List<Integer> loserSubOrders = losersGroup.stream()
                .map(item -> ((Number) item.get("subOrder")).intValue())
                .sorted()
                .toList();
        assertEquals(List.of(1, 2), loserSubOrders);
        assertTrue(loserSubOrders.stream().allMatch(value -> value > 0));
    }
}
