package io.github.akinicchi.quarkus_social.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.akinicchi.quarkus_social.rest.dto.input.CreateUserRequest;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceTest {

    ObjectMapper objectMapper;

    @TestHTTPResource("/users")
    URL apiUrl;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
    }

    @Test
    @Order(1)
    void createUserTest() throws JsonProcessingException {
        var user = new CreateUserRequest();
        user.setName("Nome Fictício");
        user.setAge(30);

        Response response = given().contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(user))
                .when().post("/users")
                .then()
                .extract().response();

        assertEquals(201, response.getStatusCode());
        assertNotNull(response.jsonPath().getString("id"));
    }

    @Test
    @Order(2)
    void createUserValidationErrorTest() throws JsonProcessingException {
        var user = new CreateUserRequest();

        Response response = given().contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(user))
                .when()
                .post("/users")
                .then()
                .extract().response();

        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
        assertEquals("Validation Error", response.jsonPath().getString("message"));

        List<Map<String, String>> errors = response.jsonPath().getList("errors");
        assertNotNull(errors.get(0).get("message"));
        assertNotNull(errors.get(1).get("message"));
    }

    @Test
    @Order(3)
    void listAllUsersTest() {
        Response response = given().contentType(ContentType.JSON)
                .when().get(apiUrl)
                .then()
                .statusCode(200)
                .extract().response();

        response.then().body("isEmpty()", Matchers.is(false));
    }
}