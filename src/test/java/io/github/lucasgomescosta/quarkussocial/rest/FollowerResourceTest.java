package io.github.lucasgomescosta.quarkussocial.rest;

import io.github.lucasgomescosta.quarkussocial.domain.model.Follower;
import io.github.lucasgomescosta.quarkussocial.domain.model.User;
import io.github.lucasgomescosta.quarkussocial.domain.repository.FollowerRepository;
import io.github.lucasgomescosta.quarkussocial.domain.repository.UserRepository;
import io.github.lucasgomescosta.quarkussocial.rest.dto.FollowerRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
class FollowerResourceTest {

    @Inject
    private UserRepository userRepository;
    @Inject
    private FollowerRepository followerRepository;

    Long userId;
    Long followerId;

    @BeforeEach
    @Transactional
    public void setUP() {
        //user padrão dos testes
        var user = new User();
        user.setAge(30);
        user.setName("fulano");

        userRepository.persist(user);
        userId = user.getId();

        // seguidor
        var follower = new User();
        follower.setAge(31);
        follower.setName("cicrano");

        userRepository.persist(follower);
        followerId = follower.getId();

        //create a follower
        var followerEntity = new Follower();
        followerEntity.setFollower(follower);
        followerEntity.setUser(user);
        followerRepository.persist(followerEntity);
    }

    @Test
    @DisplayName("should return 409 when Follower Id is equal to User id")
    public void sameUserAsFollowerTest() {

        var body = new FollowerRequest();
        body.setFollowerId(userId);

        given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .pathParam("userId", userId)
                .when()
                    .put()
                .then()
                    .statusCode(Response.Status.CONFLICT.getStatusCode())
                    .body(Matchers.is("You can't follow yourself"));
    }

    @Test
    @DisplayName("should return 404 on follower a user when User id doesn't exist ")
    public void userNotFoundTest() {

        var body = new FollowerRequest();
        body.setFollowerId(userId);
        var inexistentUserId = 999;

        given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .pathParam("userId", inexistentUserId)
                .when()
                    .put()
                .then()
                    .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should follow a user")
    public void followuserTest() {

        var body = new FollowerRequest();
        body.setFollowerId(followerId);

        given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .pathParam("userId", userId)
                .when()
                    .put()
                .then()
                    .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @DisplayName("should return 404 on list user followers and User id doesn't exist ")
    public void userNotFoundWhenListingFollowersTest() {

        var inexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", inexistentUserId)
                .when()
                .get()
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should list a user's followers")
    public void listFollowersTest() {

        var response =
            given()
                        .contentType(ContentType.JSON)
                        .pathParam("userId", userId)
                    .when()
                        .get()
                    .then()
                    .extract().response();

        var followersCount = response.jsonPath().get("followersCount");
        var followersContent = response.jsonPath().getList("content");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
        assertEquals(1, followersCount);
        assertEquals(1, followersContent.size());
    }

    @Test
    @DisplayName("should return 404 on unfollow user and User id doesn't exist ")
    public void userNotFoundWhenUnfollowingAUserTest() {

        var inexistentUserId = 999;


        given()
                    .pathParam("userId", inexistentUserId)
                    .queryParam("followerId", followerId)
                .when()
                    .delete()
                .then()
                    .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should Unfollow an user")
    public void unFollowUserTest() {

        var inexistentUserId = 999;


        given()
                .pathParam("userId", userId)
                .queryParam("followerId", followerId)
                .when()
                .delete()
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }
}