package io.github.akinicchi.quarkus_social.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.akinicchi.quarkus_social.domain.entity.Follower;
import io.github.akinicchi.quarkus_social.domain.entity.Post;
import io.github.akinicchi.quarkus_social.domain.entity.User;
import io.github.akinicchi.quarkus_social.domain.repository.FollowerRepository;
import io.github.akinicchi.quarkus_social.domain.repository.PostRepository;
import io.github.akinicchi.quarkus_social.domain.repository.UserRepository;
import io.github.akinicchi.quarkus_social.rest.dto.input.CreatePostRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

@QuarkusTest
class PostResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    FollowerRepository followerRepository;

    @Inject
    PostRepository postRepository;

    ObjectMapper objectMapper;
    Long userID;
    Long nonExistentUserID = 999L;
    Long userNotFollowerId;
    Long userFollowerId;


    @BeforeEach
    @Transactional
    void setUp() {
        this.objectMapper = new ObjectMapper();

        var user = new User();
        user.setName("Nome fictício");
        user.setAge(30);
        this.userRepository.persist(user);
        this.userID = user.getId();

        this.postRepository.persist(new Post("NEW POST TO TEST.", user));

        var userNotFollower = new User();
        userNotFollower.setAge(33);
        userNotFollower.setName("Nome fictício 2");
        this.userRepository.persist(userNotFollower);
        this.userNotFollowerId = userNotFollower.getId();

        var userFollower = new User();
        userFollower.setAge(33);
        userFollower.setName("Nome fictício 3");
        this.userRepository.persist(userFollower);
        this.userFollowerId = userFollower.getId();

        this.followerRepository.persist(new Follower(user, userFollower));
    }

    @Test
    void createPostTest() throws JsonProcessingException {
        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text.");

        given().contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(postRequest))
                .when().post("/posts/" + userID)
                .then().statusCode(201);
    }

    @Test
    void postForAnNonExistentUserTest() throws JsonProcessingException {
        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text.");

        given().contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(postRequest))
                .when().post("/posts/" + nonExistentUserID)
                .then().statusCode(404);
    }

    @Test
    void listPostUserNotFoundTest() {
        given().contentType(ContentType.JSON)
                .header("followerId", userFollowerId)
                .when().get("/posts/" + nonExistentUserID)
                .then().statusCode(404);
    }

    @Test
    void listPostFollowerHeaderNotFoundTest() {
        given().contentType(ContentType.JSON)
                .header("followerId", nonExistentUserID)
                .when().get("/posts/" + userID)
                .then().statusCode(400);
    }

    @Test
    void listPostNotAFollower() {
        given().contentType(ContentType.JSON)
                .header("followerId", userNotFollowerId)
                .when().get("/posts/" + userID)
                .then().statusCode(403);
    }

    @Test
    void listPostsTest() {
        given().contentType(ContentType.JSON)
                .header("followerId", userFollowerId)
                .when().get("/posts/" + userID)
                .then().statusCode(200);
    }
}