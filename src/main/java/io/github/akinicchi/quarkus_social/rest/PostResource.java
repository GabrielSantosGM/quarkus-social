package io.github.akinicchi.quarkus_social.rest;

import io.github.akinicchi.quarkus_social.domain.entity.Post;
import io.github.akinicchi.quarkus_social.domain.entity.User;
import io.github.akinicchi.quarkus_social.domain.repository.FollowerRepository;
import io.github.akinicchi.quarkus_social.domain.repository.PostRepository;
import io.github.akinicchi.quarkus_social.domain.repository.UserRepository;
import io.github.akinicchi.quarkus_social.rest.dto.input.CreatePostRequest;
import io.github.akinicchi.quarkus_social.rest.dto.output.PostResponse;
import io.github.akinicchi.quarkus_social.rest.dto.output.ResponseError;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.akinicchi.quarkus_social.utils.CompareUtils.isEqualsId;

@Path("/posts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostResource {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FollowerRepository followerRepository;
    private final Validator validator;

    @Inject
    public PostResource(UserRepository userRepository, PostRepository postRepository, FollowerRepository followerRepository, Validator validator) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.followerRepository = followerRepository;
        this.validator = validator;
    }

    @POST
    @Transactional
    @Path("{userId}")
    public Response savePost(@PathParam("userId") Long userId, CreatePostRequest postRequest) {
        User user = userRepository.findById(userId);
        if (Objects.isNull(user)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(postRequest);
        if (!violations.isEmpty()) {
            ResponseError responseError = ResponseError.createFromValidation(violations);
            return Response.status(Response.Status.BAD_REQUEST).entity(responseError).build();
        }

        Post post = new Post(postRequest.getText(), user);
        postRepository.persist(post);

        return Response.status(Response.Status.CREATED).entity(post).build();
    }

    @GET
    @Path("{userId}")
    public Response listPosts(@PathParam("userId") Long userId, @HeaderParam("followerId") Long followerId) {
        if (isEqualsId(userId, followerId)) return Response.status(Response.Status.BAD_REQUEST).build();

        User user = userRepository.findById(userId);
        if (Objects.isNull(user)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        User follower = userRepository.findById(followerId);

        if (Objects.isNull(follower)) return Response.status(Response.Status.BAD_REQUEST).build();

        if (!followerRepository.isFollowing(follower, user)) return Response.status(Response.Status.FORBIDDEN).build();

        PanacheQuery<Post> recoveredPosts = postRepository.find("user", Sort.by("dateTime", Sort.Direction.Descending), user);
        if (recoveredPosts.list().isEmpty()) return Response.status(Response.Status.NO_CONTENT).build();

        List<PostResponse> response = recoveredPosts.stream().map(PostResponse::fromEntity).collect(Collectors.toList());
        return Response.ok(response).build();
    }
}