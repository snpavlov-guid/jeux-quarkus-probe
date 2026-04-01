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

    @Test
    public void createTeamReturnsValidationErrorWhenNameExceedsColumnLength() {
        authorized()
                .contentType("application/json")
                .body(Map.of(
                        "name", longerThan(128),
                        "shortName", "AB",
                        "city", "c",
                        "logoUrl", "http://u"
                ))
                .when()
                .post("/api/q/v1/teams/create")
                .then()
                .statusCode(200)
                .body("result", equalTo(false))
                .body("message", equalTo("Ошибка валидации"))
                .body("validations", hasSize(1))
                .body("validations[0].property", equalTo("name"))
                .body("validations[0].message", equalTo(expectedTooLongMessage(128)));
    }

    @Test
    public void createTeamReturnsValidationErrorWhenShortNameExceedsColumnLength() {
        authorized()
                .contentType("application/json")
                .body(Map.of(
                        "name", "Ok",
                        "shortName", longerThan(6),
                        "city", "c",
                        "logoUrl", "http://u"
                ))
                .when()
                .post("/api/q/v1/teams/create")
                .then()
                .statusCode(200)
                .body("result", equalTo(false))
                .body("message", equalTo("Ошибка валидации"))
                .body("validations", hasSize(1))
                .body("validations[0].property", equalTo("shortName"))
                .body("validations[0].message", equalTo(expectedTooLongMessage(6)));
    }

    @Test
    public void updateTeamReturnsValidationErrorWhenCityExceedsColumnLength() {
        Integer id = authorized()
                .contentType("application/json")
                .body(Map.of(
                        "name", "V Team",
                        "shortName", "VT",
                        "city", "c",
                        "logoUrl", "http://u"
                ))
                .when()
                .post("/api/q/v1/teams/create")
                .then()
                .statusCode(200)
                .body("result", equalTo(true))
                .extract()
                .path("data.id");

        try {
            authorized()
                    .contentType("application/json")
                    .body(Map.of(
                            "id", id,
                            "name", "V Team",
                            "shortName", "VT",
                            "city", longerThan(128),
                            "logoUrl", "http://u"
                    ))
                    .when()
                    .post("/api/q/v1/teams/update/" + id)
                    .then()
                    .statusCode(200)
                    .body("result", equalTo(false))
                    .body("message", equalTo("Ошибка валидации"))
                    .body("validations[0].property", equalTo("city"))
                    .body("validations[0].message", equalTo(expectedTooLongMessage(128)));
        } finally {
            authorized()
                    .contentType("application/json")
                    .body("{}")
                    .when()
                    .post("/api/q/v1/teams/delete/" + id)
                    .then()
                    .statusCode(200);
        }
    }

}
