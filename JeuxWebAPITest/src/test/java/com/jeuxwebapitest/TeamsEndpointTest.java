package com.jeuxwebapitest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.jeuxwebapitest.util.KeycloakAuthUtil;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TeamsEndpointTest {
    private static String authToken;

    @BeforeAll
    static void initAuthToken() {
        authToken = KeycloakAuthUtil.getKeycloakAuthToken();
    }

    private static io.restassured.specification.RequestSpecification authorized() {
        return given().header("Authorization", "Bearer " + authToken);
    }

    @Test
    public void teamsEndpointSupportsCommonParameters() {
        authorized()
                .when()
                .get("/api/q/v1/teams")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .body("items", notNullValue())
                .body("total", greaterThanOrEqualTo(0));

        authorized()
                .queryParam("skip", 0)
                .queryParam("size", 5)
                .when()
                .get("/api/q/v1/teams")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        authorized()
                .queryParam("name", "a")
                .when()
                .get("/api/q/v1/teams")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));

        authorized()
                .queryParam("order", "desc")
                .when()
                .get("/api/q/v1/teams")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));
    }

    @Test
    public void teamsEndpointReturnsEntityById() {
        List<Integer> ids = authorized()
                .when()
                .get("/api/q/v1/teams")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .extract()
                .path("items.id");

        if (ids != null && !ids.isEmpty()) {
            Integer id = ids.get(0);
            authorized()
                    .when()
                    .get("/api/q/v1/teams/" + id)
                    .then()
                    .statusCode(200)
                    .body("result", equalTo(true))
                    .body("data.id", equalTo(id));
        }

        authorized()
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
        Integer id = authorized()
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

        authorized()
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

        authorized()
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
