package io.github.lucasgomescosta.quarkussocial.rest;

import io.github.lucasgomescosta.quarkussocial.domain.model.Follower;
import io.github.lucasgomescosta.quarkussocial.domain.model.User;
import io.github.lucasgomescosta.quarkussocial.domain.repository.FollowerRepository;
import io.github.lucasgomescosta.quarkussocial.domain.repository.UserRepository;
import io.github.lucasgomescosta.quarkussocial.rest.dto.FollowerRequest;
import io.github.lucasgomescosta.quarkussocial.rest.dto.FollowerResponse;
import io.github.lucasgomescosta.quarkussocial.rest.dto.FollowersPerUserResponse;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

@Path("/users/{userId}/followers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FollowerResource {

    private FollowerRepository repository;
    private UserRepository userRepository;

    @Inject
    public FollowerResource(FollowerRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @PUT
    @Transactional
    public Response followerUser(
            @PathParam("userId") Long userId, FollowerRequest followerRequest) {

        if(userId.equals(followerRequest.getFollowerId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("You can't follow yourself")
                    .build();
        }

        var user = userRepository.findById(userId);
        if(user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var follower = userRepository.findById(followerRequest.getFollowerId());

        boolean follows = repository.follows(follower, user);

        if(!follows) {
            var entity = new Follower();
            entity.setUser(user);
            entity.setFollower(follower);

            repository.persist(entity);
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }


    @GET
    public Response listFollowers(
            @PathParam("userId") Long userId,
            @HeaderParam("followerId") Long followerId) {

        var user = userRepository.findById(userId);
        if(user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var list = repository.findByUser(userId);
        FollowersPerUserResponse userResponse = new FollowersPerUserResponse();
        userResponse.setFollowersCount(list.size());

        var followerList = list.stream().map(FollowerResponse::new ).collect(Collectors.toList());

        userResponse.setContent(followerList);
        return Response.ok(userResponse).build();
    }

    @DELETE
    @Transactional
    public Response unFollowerUser(
            @PathParam("userId") Long userId,
            @QueryParam("followerId") Long followerId) {

        var user = userRepository.findById(userId);
        if(user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        repository.deleteByFollowerAndUser(followerId, userId);

        return Response.status(Response.Status.NO_CONTENT).build();
    }


}
