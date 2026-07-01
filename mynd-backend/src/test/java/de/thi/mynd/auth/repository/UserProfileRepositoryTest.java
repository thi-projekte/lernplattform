/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.auth.repository;

import static org.junit.jupiter.api.Assertions.*;

import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.common.entity.CreatorIdKey;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link UserProfileRepository} against a real Postgres instance (Quarkus dev services).
 * Usernames are randomized per fixture to avoid collisions with demo content / other tests running
 * against the same database.
 */
@QuarkusTest
class UserProfileRepositoryTest {

  @Inject UserProfileRepository userProfileRepository;

  private UserProfile newUserProfile(String username) {
    UserProfile profile = new UserProfile();
    // The EmbeddedId's creatorId column is insertable=false/updatable=false, so the actual
    // column value is driven by the inherited BaseEntity.creatorId field. Both must be set
    // explicitly to keep the embeddable and the persisted column in sync.
    CreatorIdKey id = new CreatorIdKey();
    id.creatorId = username;
    profile.id = id;
    profile.creatorId = username;
    profile.invitationsLeft = 0;
    return profile;
  }

  private String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  @Test
  @TestTransaction
  void findByUsernameOptional_existingUsername_returnsProfile() {
    String username = unique("alice");
    userProfileRepository.persistAndFlush(newUserProfile(username));

    Optional<UserProfile> result = userProfileRepository.findByUsernameOptional(username);

    assertTrue(result.isPresent());
    assertEquals(username, result.get().creatorId);
    assertEquals(username, result.get().id.creatorId);
  }

  @Test
  @TestTransaction
  void findByUsernameOptional_noMatch_returnsEmpty() {
    Optional<UserProfile> result = userProfileRepository.findByUsernameOptional(unique("ghost"));

    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void findAllWithLimit_respectsLimit() {
    String marker = unique("limit-user");
    for (int i = 0; i < 5; i++) {
      userProfileRepository.persistAndFlush(newUserProfile(marker + "-" + i));
    }

    List<UserProfile> result = userProfileRepository.findAllWithLimit(3);

    assertEquals(3, result.size());
  }

  // Note: findByIdsTypeSafe (inherited from MyndBaseCustomIdRepository) is hard-coded to accept a
  // List<UUID> regardless of the repository's actual ID type. It compiles for UserProfileRepository
  // (whose ID type is CreatorIdKey, not UUID) only because the method signature does not depend on
  // the generic ID parameter, but the underlying query ("id IN ?1") would compare the CreatorIdKey
  // primary key against UUID values, which is not a meaningful comparison for this entity. We
  // therefore intentionally do not exercise findByIdsTypeSafe here.
}
