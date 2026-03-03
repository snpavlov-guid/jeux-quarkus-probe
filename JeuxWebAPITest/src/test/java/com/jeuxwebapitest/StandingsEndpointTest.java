package com.jeuxwebapitest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.jeuxwebapitest.util.H2FlywayTestResource;
import com.jeuxwebapitest.util.KeycloakAuthUtil;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
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
                .body("total", equalTo(16))
                .body("items.size()", equalTo(16));
    }
}
