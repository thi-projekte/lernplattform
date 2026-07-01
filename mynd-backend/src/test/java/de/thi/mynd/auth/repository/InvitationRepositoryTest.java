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

import de.thi.mynd.auth.entity.Invitation;
import de.thi.mynd.common.dto.PaginationDto;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link InvitationRepository} against a real Postgres instance (Quarkus dev services).
 * Every fixture creatorId is randomized to avoid false positives from pre-existing rows.
 */
@QuarkusTest
class InvitationRepositoryTest {

  @Inject InvitationRepository invitationRepository;

  private String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  private Invitation newInvitation(String creatorId, String mailSentTo) {
    Invitation invitation = new Invitation();
    invitation.creatorId = creatorId;
    invitation.mailSentTo = mailSentTo;
    invitation.redemptionSecret = UUID.randomUUID().toString();
    return invitation;
  }

  @Test
  @TestTransaction
  void getInvitationsForCreatorPaginated_returnsOnlyOwnInvitationsPaginated() {
    String creatorId = unique("creator");
    Invitation a = newInvitation(creatorId, "a@example.com");
    Invitation b = newInvitation(creatorId, "b@example.com");
    Invitation c = newInvitation(creatorId, "c@example.com");
    invitationRepository.persistAndFlush(a);
    invitationRepository.persistAndFlush(b);
    invitationRepository.persistAndFlush(c);

    Invitation other = newInvitation(unique("other-creator"), "other@example.com");
    invitationRepository.persistAndFlush(other);

    PaginationDto<Invitation> firstPage =
        invitationRepository.getInvitationsForCreatorPaginated(creatorId, 0, 2);
    PaginationDto<Invitation> secondPage =
        invitationRepository.getInvitationsForCreatorPaginated(creatorId, 1, 2);

    assertEquals(2, firstPage.results.size());
    assertEquals(1, secondPage.results.size());
    assertEquals(2, firstPage.totalPages);
    assertTrue(firstPage.results.stream().allMatch(i -> i.creatorId.equals(creatorId)));
    assertTrue(secondPage.results.stream().allMatch(i -> i.creatorId.equals(creatorId)));
    List<UUID> allIds =
        List.of(
            firstPage.results.get(0).id, firstPage.results.get(1).id, secondPage.results.get(0).id);
    assertTrue(allIds.stream().noneMatch(id -> id.equals(other.id)));
  }

  @Test
  @TestTransaction
  void getInvitationsForCreatorPaginated_noInvitations_returnsEmptyPage() {
    PaginationDto<Invitation> result =
        invitationRepository.getInvitationsForCreatorPaginated(unique("lonely-creator"), 0, 10);

    assertTrue(result.results.isEmpty());
    // Panache's pageCount() special-cases a zero-row result as a single (empty) page, not zero
    // pages.
    assertEquals(1, result.totalPages);
  }

  @Test
  @TestTransaction
  void getAmountInvitationsSubmittedPerUser_countsOnlyThatUsersInvitations() {
    String creatorId = unique("creator");
    invitationRepository.persistAndFlush(newInvitation(creatorId, "a@example.com"));
    invitationRepository.persistAndFlush(newInvitation(creatorId, "b@example.com"));
    invitationRepository.persistAndFlush(newInvitation(unique("other-creator"), "c@example.com"));

    long result = invitationRepository.getAmountInvitationsSubmittedPerUser(creatorId);

    assertEquals(2, result);
  }

  @Test
  @TestTransaction
  void getAmountInvitationsSubmittedPerUser_noInvitations_returnsZero() {
    long result =
        invitationRepository.getAmountInvitationsSubmittedPerUser(unique("no-invitations"));

    assertEquals(0, result);
  }
}
