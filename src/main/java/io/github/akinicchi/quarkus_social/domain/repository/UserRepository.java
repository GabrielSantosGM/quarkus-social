package io.github.akinicchi.quarkus_social.domain.repository;

import io.github.akinicchi.quarkus_social.domain.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
}