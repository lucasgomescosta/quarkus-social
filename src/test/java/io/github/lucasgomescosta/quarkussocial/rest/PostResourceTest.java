package io.github.lucasgomescosta.quarkussocial.rest;

import io.github.lucasgomescosta.quarkussocial.domain.model.Follower;
import io.github.lucasgomescosta.quarkussocial.domain.model.Post;
import io.github.lucasgomescosta.quarkussocial.domain.model.User;
import io.github.lucasgomescosta.quarkussocial.domain.repository.FollowerRepository;
import io.github.lucasgomescosta.quarkussocial.domain.repository.PostRepository;
import io.github.lucasgomescosta.quarkussocial.domain.repository.UserRepository;
import io.github.lucasgomescosta.quarkussocial.rest.dto.CreatePostRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(PostResource.class)
class PostResourceTest {

    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;
    @Inject
    PostRepository postRepository;

    Long userId;
    Long userNotFollowerId;
    Long userFollowerId;

    @BeforeEach
    @Transactional
    public void setUP() {
        //user padrão dos testes
        var user = new User();
        user.setAge(30);
        user.setName("fulano");

        userRepository.persist(user);
        userId = user.getId();

        //criada a postagem para o user
        Post post = new Post();
        post.setText("Hello");
        post.setUser(user);
        postRepository.persist(post);

        //user que não segue ninguém
        var userNotFollower = new User();
        userNotFollower.setAge(33);
        userNotFollower.setName("cicrano");

        userRepository.persist(userNotFollower);
        userNotFollowerId = userNotFollower.getId();

        //user seguidor
        var userFollower = new User();
        userFollower.setAge(33);
        userFollower.setName("cicrano");

        userRepository.persist(userFollower);
        userFollowerId = userFollower.getId();

        Follower follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);
        followerRepository.persist(follower);

    }

    @Test
    @DisplayName("should create a post for a user")
    public void createPostTest() {

        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");

        given()
                    .contentType(ContentType.JSON)
                    .body(postRequest)
                    .pathParam("userId", userId)
                .when()
                    .post()
                .then()
                    .statusCode(201);
    }


    @Test
    @DisplayName("should return 404 when trying to make a post for an inexistent user")
    public void PostForAnInexistentUserTest() {

        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");

        var indexistentUserID = 999;

        given()
                .contentType(ContentType.JSON)
                .body(postRequest)
                .pathParam("userId", indexistentUserID)
                .when()
                .post()
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("should return 404 when user doesn't exist")
    public void listPostUserNotFoundTest() {
        var inexistentUserId = 999;

        given()
                    .pathParam("userId", inexistentUserId)
                .when()
                    .get()
                .then()
                    .statusCode(404);
    }

    @Test
    @DisplayName("should return 400 when followerId header is not present")
    public void listPostFollowerHeaderNotSendTest() {
        given()
                    .pathParam("userId", userId)
                .when()
                    .get()
                .then()
                    .statusCode(400)
                    .body(Matchers.is("You forgot the header followerId"));
    }

    @Test
    @DisplayName("should return 400 when follower doesn't exist")
    public void listPostFollowerHeaderNotFoundTest() {
        var inexistentFollowerId = 999;

        given()
                    .pathParam("userId", userId)
                .header("followerId", inexistentFollowerId)
                .when()
                    .get()
                .then()
                    .statusCode(400)
                    .body(Matchers.is("Inexistent followerId"));
    }

    @Test
    @DisplayName("should return 403 when follower isn't follower")
    public void listPostNotAFollowerTest() {
        given()
                    .pathParam("userId", userId)
                    .header("followerId", userNotFollowerId)
                .when()
                    .get()
                .then()
                    .statusCode(403)
                    .body(Matchers.is("You can't see these posts"));
    }

    @Test
    @DisplayName("should return posts")
    public void listPostTest() {
        given()
                    .pathParam("userId", userId)
                    .header("followerId", userFollowerId)
                .when()
                    .get()
                .then()
                    .statusCode(200)
                    .body("size()", Matchers.is(1));
    }
}