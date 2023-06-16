package io.github.akinicchi.quarkus_social.rest.dto.input;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePostRequest {

    @NotBlank(message = "Escreve uma mensagem legal ai pô! :)")
    private String text;
}