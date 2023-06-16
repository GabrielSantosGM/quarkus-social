package io.github.akinicchi.quarkus_social.rest;

import io.github.akinicchi.quarkus_social.domain.entity.Follower;
import io.github.akinicchi.quarkus_social.domain.entity.User;
import io.github.akinicchi.quarkus_social.domain.repository.FollowerRepository;
import io.github.akinicchi.quarkus_social.domain.repository.UserRepository;
import io.github.akinicchi.quarkus_social.rest.dto.input.FollowerRequest;
import io.github.akinicchi.quarkus_social.rest.dto.output.FollowerResponse;
import io.github.akinicchi.quarkus_social.rest.dto.output.FollowersPerUserResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.akinicchi.quarkus_social.utils.CompareUtils.isEqualsId;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/followers")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class FollowerResource {

    private final FollowerRepository followerRepository;
    private final UserRepository userRepository;

    @Inject
    public FollowerResource(FollowerRepository followerRepository, UserRepository userRepository) {
        this.followerRepository = followerRepository;
        this.userRepository = userRepository;
    }

    @PUT
    @Transactional
    @Path("{userId}")
    public Response followUser(@PathParam("userId") Long userId, FollowerRequest followerRequest) {
        if (isEqualsId(userId, followerRequest.getFollowerId()))
            return Response.status(Response.Status.BAD_REQUEST).build();

        User user = userRepository.findById(userId);
        if (Objects.isNull(user)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        User follower = userRepository.findById(followerRequest.getFollowerId());

        if (followerRepository.isFollowing(follower, user)) return Response.status(Response.Status.NO_CONTENT).build();

        Follower toSave = new Follower(user, follower);
        followerRepository.persist(toSave);

        return Response.ok(toSave).build();
    }

    @GET
    @Path("{userId}")
    public Response listFollowers(@PathParam("userId") Long userId) {
        User user = userRepository.findById(userId);
        if (Objects.isNull(user)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<Follower> followers = followerRepository.findByUser(userId);
        FollowersPerUserResponse response = new FollowersPerUserResponse();
        response.setFollowersCount(followers.size());

        List<FollowerResponse> content = followers.stream()
                .map(FollowerResponse::new)
                .collect(Collectors.toList());

        response.setContent(content);
        return Response.ok(response).build();
    }

    @DELETE
    @Transactional
    @Path("{userId}")
    public Response unfollowUser(@PathParam("userId") Long userId, @QueryParam("followerId") Long followerId) {
        if (isEqualsId(userId, followerId)) return Response.status(Response.Status.BAD_REQUEST).build();

        User user = userRepository.findById(userId);
        if (Objects.isNull(user)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        followerRepository.deleteByFollowerAndUser(followerId, userId);

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}