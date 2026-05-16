package de.thi.mynd.auth.repository;

import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.common.repository.MyndBaseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public final class UserProfileRepository extends MyndBaseRepository<UserProfile> {

  public Optional<UserProfile> findByUsername(String username) {
    return find("creatorId", username).firstResultOptional();
  }
}