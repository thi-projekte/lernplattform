package de.thi.mynd.auth.repository;

import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.common.repository.MyndBaseCustomIdRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public final class UserProfileRepository extends MyndBaseCustomIdRepository<UserProfile, CreatorIdKey> {

  public Optional<UserProfile> findByUsername(String username) {
    return find("id.creatorId = ?1", username).singleResultOptional();
  }
}
