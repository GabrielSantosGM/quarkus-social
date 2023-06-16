package io.github.akinicchi.quarkus_social.rest;

import io.github.akinicchi.quarkus_social.domain.entity.User;
import io.github.akinicchi.quarkus_social.domain.repository.UserRepository;
import io.github.akinicchi.quarkus_social.rest.dto.input.CreateUserRequest;
import io.github.akinicchi.quarkus_social.rest.dto.output.ResponseError;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Objects;
import java.util.Set;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private final UserRepository repository;
    private final Validator validator;

    @Inject
    public UserResource(UserRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @POST
    @Transactional
    public Response createUser(CreateUserRequest userRequest) {
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(userRequest);
        if (!violations.isEmpty()) {
            ResponseError responseError = ResponseError.createFromValidation(violations);
            return Response.status(Response.Status.BAD_REQUEST).entity(responseError).build();
        }

        User user = User.builder()
                .name(userRequest.getName())
                .age(userRequest.getAge())
                .build();

        repository.persist(user);
        return Response
                .status(Response.Status.CREATED)
                .entity(user)
                .build();
    }

    @GET
    public Response listAllUsers() {
        PanacheQuery<User> allUsers = repository.findAll();
        if (allUsers.list().isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(allUsers.list()).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response deleteUser(@PathParam("id") Long id) {
        User user = repository.findById(id);
        if (Objects.nonNull(user)) {
            repository.delete(user);
            return Response.noContent().build();
        }
        return Response.status(404).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response updateUser(@PathParam("id") Long id, CreateUserRequest userRequest) {
        User user = repository.findById(id);
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(userRequest);
        if (!violations.isEmpty()) {
            ResponseError responseError = ResponseError.createFromValidation(violations);
            return Response.status(Response.Status.BAD_REQUEST).entity(responseError).build();
        }

        if (Objects.nonNull(user)) {
            user.setName(userRequest.getName());
            user.setAge(userRequest.getAge());
            return Response.ok(user).build();
        }

        return Response.status(404).build();
    }
}