package com.jeuxwebapitest;

import static com.jeuxwebapitest.util.ValidationTestSupport.expectedTooLongMessage;
import static com.jeuxwebapitest.util.ValidationTestSupport.longerThan;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.jeuxwebapitest.util.KeycloakAuthUtil;
import com.jeuxwebapitest.util.H2FlywayTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(H2FlywayTestResource.class)
public class LeaguesEndpointTest {
    private static String authToken;

    @BeforeAll
    static void initAuthToken() {
        authToken = KeycloakAuthUtil.getKeycloakAuthToken();
    }

    private static io.restassured.specification.RequestSpecification authorized() {
        return given().header("Authorization", "Bearer " + authToken);
    }

    @Test
    public void leaguesEndpointSupportsCommonParameters() {
        authorized()
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("items", notNullValue())
                .body("total", greaterThanOrEqualTo(0));

        authorized()
                .queryParam("skip", 0)
                .queryParam("size", 5)
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        authorized()
                .queryParam("name", "a")
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        authorized()
                .queryParam("order", "desc")
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));
    }

    @Test
    public void leaguesEndpointReturnsEntityById() {
        List<Integer> ids = authorized()
                .when()
                .get("/api/q/v1/leagues")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .extract()
                .path("items.id");

        if (ids != null && !ids.isEmpty()) {
            Integer id = ids.get(0);
            authorized()
                    .when()
                    .get("/api/q/v1/leagues/" + id)
                    .then()
                    .statusCode(200)
                    .body("result", equalTo(true))
                    .body("data.id", equalTo(id));
        }

        authorized()
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
        Integer id = authorized()
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

        authorized()
                .contentType("application/json")
                .body(Map.of("id", id, "name", "Test League Updated"))
                .when()
                .post("/api/q/v1/leagues/update/" + id)
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("data.id", equalTo(id))
                .body("data.name", equalTo("Test League Updated"));

        authorized()
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
        authorized()
                .when()
                .get("/api/q/v1/leagues/rpl")
                .then()
                .statusCode(200);
    }

    @Test
    public void createLeagueReturnsValidationErrorWhenNameExceedsColumnLength() {
        authorized()
                .contentType("application/json")
                .body(Map.of("name", longerThan(128)))
                .when()
                .post("/api/q/v1/leagues/create")
                .then()
                .statusCode(200)
                .body("result", equalTo(false))
                .body("message", equalTo("Ошибка валидации"))
                .body("validations", hasSize(1))
                .body("validations[0].property", equalTo("name"))
                .body("validations[0].message", equalTo(expectedTooLongMessage(128)));
    }

    @Test
    public void updateLeagueReturnsValidationErrorWhenNameExceedsColumnLength() {
        Integer id = authorized()
                .contentType("application/json")
                .body(Map.of("name", "League For Length Test"))
                .when()
                .post("/api/q/v1/leagues/create")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .extract()
                .path("data.id");

        try {
            authorized()
                    .contentType("application/json")
                    .body(Map.of("id", id, "name", longerThan(128)))
                    .when()
                    .post("/api/q/v1/leagues/update/" + id)
                    .then()
                    .statusCode(200)
                    .body("result", equalTo(false))
                    .body("message", equalTo("Ошибка валидации"))
                    .body("validations[0].property", equalTo("name"))
                    .body("validations[0].message", equalTo(expectedTooLongMessage(128)));
        } finally {
            authorized()
                    .contentType("application/json")
                    .body("{}")
                    .when()
                    .post("/api/q/v1/leagues/delete/" + id)
                    .then()
                    .statusCode(200);
        }
    }

}
