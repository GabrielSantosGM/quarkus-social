package io.github.akinicchi.quarkus_social.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.akinicchi.quarkus_social.domain.entity.Follower;
import io.github.akinicchi.quarkus_social.domain.entity.User;
import io.github.akinicchi.quarkus_social.domain.repository.FollowerRepository;
import io.github.akinicchi.quarkus_social.domain.repository.UserRepository;
import io.github.akinicchi.quarkus_social.rest.dto.input.FollowerRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class FollowerResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    FollowerRepository followerRepository;

    Long userID;
    Long followerID;
    Long nonExistentUserID;
    ObjectMapper objectMapper;

    @BeforeEach
    @Transactional
    void setUp() {
        this.objectMapper = new ObjectMapper();

        this.nonExistentUserID = 999L;

        var user = new User();
        user.setName("Nome fictício");
        user.setAge(30);
        this.userRepository.persist(user);
        this.userID = user.getId();

        var follower = new User();
        follower.setName("Nome fictício");
        follower.setAge(30);
        this.userRepository.persist(follower);
        this.followerID = follower.getId();

        var followerEntity = new Follower();
        followerEntity.setFollower(follower);
        followerEntity.setUser(user);
        followerRepository.persist(followerEntity);
    }

    @Test
    void sameUserAsFollowerTest() throws JsonProcessingException {
        var body = new FollowerRequest();
        body.setFollowerId(userID);

        given().contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(body))
                .when().put("/followers/" + userID)
                .then().statusCode(400);
    }

    @Test
    void userNotFoundTest() throws JsonProcessingException {
        var body = new FollowerRequest();
        body.setFollowerId(userID);

        given().contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(body))
                .when().put("/followers/" + nonExistentUserID)
                .then().statusCode(404);
    }

    @Test
    void followerUserTest() throws JsonProcessingException {
        var body = new FollowerRequest();
        body.setFollowerId(followerID);

        given().contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(body))
                .when().put("/followers/" + userID)
                .then().statusCode(204);
    }

    @Test
    void userNotFoundWhenListingFollowersTest() {
        given().contentType(ContentType.JSON)
                .when().get("/followers/" + nonExistentUserID)
                .then().statusCode(404);
    }

    @Test
    void listFollowersTest() {
        Response response = given().contentType(ContentType.JSON)
                .when().get("/followers/" + userID)
                .then().extract().response();

        Integer followersCount = response.jsonPath().get("followersCount");

        assertEquals(OK.getStatusCode(), response.getStatusCode());
        assertEquals(1, followersCount);
    }

    @Test
    void userNotFoundWhenUnfollowingAUserTest() {
        given().contentType(ContentType.JSON)
                .queryParam("followerId", followerID)
                .when().delete("/followers/" + nonExistentUserID)
                .then().statusCode(404);
    }

    @Test
    void unfollowUserTest() {
        given().contentType(ContentType.JSON)
                .queryParam("followerId", followerID)
                .when().delete("/followers/" + userID)
                .then().statusCode(204);
    }
}