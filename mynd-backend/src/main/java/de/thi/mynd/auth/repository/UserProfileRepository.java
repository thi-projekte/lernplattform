package de.thi.mynd.auth.repository;

import de.thi.mynd.auth.entity.UserProfile;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public final class UserProfileRepository implements PanacheRepositoryBase<UserProfile, String> {

  public Optional<UserProfile> findByUsername(String username) {
    return findByIdOptional(username);
  }
}
