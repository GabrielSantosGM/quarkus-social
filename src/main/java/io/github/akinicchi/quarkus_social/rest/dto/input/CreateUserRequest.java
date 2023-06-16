package io.github.akinicchi.quarkus_social.rest.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Name is required.")
    private String name;

    @NotNull(message = "Age is required.")
    private Integer age;
}