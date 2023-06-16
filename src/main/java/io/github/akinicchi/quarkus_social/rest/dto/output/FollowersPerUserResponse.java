package io.github.akinicchi.quarkus_social.rest.dto.output;

import lombok.Data;

import java.util.List;

@Data
public class FollowersPerUserResponse {

    private Integer followersCount;
    private List<FollowerResponse> content;
}
