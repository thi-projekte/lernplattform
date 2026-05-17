package de.thi.mynd.auth.repository;

import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.common.repository.MyndBaseCustomIdRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public final class UserProfileRepository extends MyndBaseCustomIdRepository<UserProfile, String> {

  public Optional<UserProfile> findByUsername(String username) {
    return findByIdOptional(username);
  }
}
