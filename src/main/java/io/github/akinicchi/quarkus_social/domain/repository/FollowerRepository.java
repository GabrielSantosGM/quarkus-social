package io.github.akinicchi.quarkus_social.domain.repository;

import io.github.akinicchi.quarkus_social.domain.entity.Follower;
import io.github.akinicchi.quarkus_social.domain.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class FollowerRepository implements PanacheRepository<Follower> {

    public boolean isFollowing(User follower, User user) {
        Map<String, Object> params = Parameters
                .with("follower", follower)
                .and("user", user)
                .map();
        PanacheQuery<Follower> query = find("follower =:follower and user =:user", params);
        return query.firstResultOptional().isPresent();
    }

    public List<Follower> findByUser(Long userId) {
        PanacheQuery<Follower> query = find("user.id", userId);
        return query.list();
    }

    public void deleteByFollowerAndUser(Long followerId, Long userId) {
        Map<String, Object> params = Parameters
                .with("userId", userId)
                .and("followerId", followerId)
                .map();

        delete("follower.id =:followerId and user.id =:userId", params);
    }
}