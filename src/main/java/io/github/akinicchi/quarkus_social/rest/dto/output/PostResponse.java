package io.github.akinicchi.quarkus_social.rest.dto.output;

import io.github.akinicchi.quarkus_social.domain.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PostResponse {

    private String text;
    private LocalDateTime dateTime;

    public static PostResponse fromEntity(Post post) {
        return new PostResponse(post.getText(), post.getDateTime());
    }
}