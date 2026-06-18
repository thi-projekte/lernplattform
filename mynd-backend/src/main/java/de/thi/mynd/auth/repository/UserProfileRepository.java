/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.auth.repository;

import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.common.repository.MyndBaseCustomIdRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public final class UserProfileRepository
    extends MyndBaseCustomIdRepository<UserProfile, CreatorIdKey> {

  public Optional<UserProfile> findByUsernameOptional(String username) {
    return find("id.creatorId = ?1", username).singleResultOptional();
  }
}
