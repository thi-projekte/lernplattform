/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.auth.service;

import de.thi.mynd.auth.dto.InvitationDto;
import de.thi.mynd.auth.dto.PersonalInvitationStatusDto;
import de.thi.mynd.auth.entity.Invitation;
import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.auth.exception.CannotAcceptInvitationException;
import de.thi.mynd.auth.exception.NoInvitationsLeftException;
import de.thi.mynd.auth.repository.InvitationRepository;
import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.service.IdentityService;
import de.thi.mynd.common.utility.TokenGenerator;
import de.thi.mynd.notification.service.GenericEmailService;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public final class InvitationServiceImpl implements InvitationService {

  @Inject InvitationRepository invitationRepository;

  @Inject MappingRegistry mappingRegistry;

  @Inject UserProfileService userProfileService;

  @Inject SecurityIdentity securityIdentity;

  @Inject IdentityService identityService;

  @Inject GenericEmailService genericEmailService;

  @ConfigProperty(name = "mynd.frontendUri")
  String frontendUri;

  @Override
  public InvitationDto getInvitation(UUID invitationId) {
    Log.tracef("Loading invitation with ID %s", invitationId);
    Invitation invitation = getInvitationDefaultThrow(invitationId);
    return mappingRegistry.map(invitation, InvitationDto.class);
  }

  @Override
  public PaginationDto<InvitationDto> getSentInvitations(int page, int pageSize) {
    String creatorId = securityIdentity.getPrincipal().getName();
    Log.tracef("Loading all sent invitations for user %s", creatorId);
    PaginationDto<Invitation> invitationPagination =
        invitationRepository.getInvitationsForCreatorPaginated(creatorId, page, pageSize);

    return PaginationDto.<InvitationDto>builder()
        .results(mappingRegistry.mapList(invitationPagination.results, InvitationDto.class))
        .totalPages(invitationPagination.totalPages)
        .build();
  }

  @Override
  public PersonalInvitationStatusDto getPersonalInvitationStatus() {
    String creatorId = securityIdentity.getPrincipal().getName();
    long alreadySent = invitationRepository.getAmountInvitationsSubmittedPerUser(creatorId);
    UserProfile userProfile = getCurrentUsersProfile();

    Log.tracef("Fetching personal invitation status for user %s", creatorId);

    return PersonalInvitationStatusDto.builder()
        .invitationsLeft(userProfile.invitationsLeft)
        .invitationsAlreadySent(alreadySent)
        .build();
  }

  @Override
  @Transactional
  public void sendInvitation(String email) {
    UserProfile userProfile = getCurrentUsersProfile();

    if (userProfile.invitationsLeft == 0) {
      throw new NoInvitationsLeftException("You do not have any invitations left");
    }

    userProfile.invitationsLeft--;
    userProfileService.updateUserProfile(userProfile);

    Invitation invitation = new Invitation();
    invitation.creatorId = userProfile.creatorId;
    invitation.mailSentTo = email;
    invitation.redemptionSecret = TokenGenerator.generateRandomString(64);
    invitationRepository.persistAndFlush(invitation);

    Log.infof(
        "Successfully created invitation for email %s as user %s", email, userProfile.creatorId);

    sendInvitationEmail(invitation);
  }

  @Override
  @Transactional
  public void redeemInvitation(UUID id, String secret) {
    Invitation invitation = getInvitationDefaultThrow(id);
    String creatorId = securityIdentity.getPrincipal().getName();

    if (Objects.equals(invitation.creatorId, creatorId)) {
      throw new CannotAcceptInvitationException("You cannot accept the invitation yourself");
    }

    if (!secret.equals(invitation.redemptionSecret)) {
      throw new CannotAcceptInvitationException("Invalid redemption secret supplied");
    }

    if (invitation.acceptedBy != null) {
      throw new CannotAcceptInvitationException(
          "This invitation expired. It has already been accepted");
    }

    invitation.acceptedBy = creatorId;
    invitationRepository.persistAndFlush(invitation);

    identityService.addRolesToUser(creatorId, List.of("authorizedUser"));

    Log.infof("Redeemed invitation for user %s", creatorId);
  }

  private void sendInvitationEmail(Invitation invitation) {
    Map<String, String> parameters =
        Map.of(
            "logoUrl",
            frontendUri + "/mynd-logo.png",
            "invitationLink",
            String.format(
                "%s/acceptInvite?id=%s&redemptionSecret=%s",
                frontendUri, invitation.id, invitation.redemptionSecret));
    genericEmailService.sendEmail(
        "invitation", "MYnd Invitation", List.of(invitation.mailSentTo), parameters);

    Log.tracef("Sent invitation message to %s", invitation.mailSentTo);
  }

  private Invitation getInvitationDefaultThrow(UUID id) {
    Optional<Invitation> invitationOptional = invitationRepository.findByIdOptional(id);

    if (invitationOptional.isEmpty()) {
      throw new EntityInstanceNotFoundException("There is no invitation with this ID");
    }

    return invitationOptional.get();
  }

  private UserProfile getCurrentUsersProfile() {
    return userProfileService
        .getPersonalUserProfile()
        .orElseGet(() -> userProfileService.createPersonalUserProfile());
  }
}
