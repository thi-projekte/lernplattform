package de.thi.mynd.auth.service;

import de.thi.mynd.auth.dto.InvitationDto;
import de.thi.mynd.auth.dto.PersonalInvitationStatusDto;
import de.thi.mynd.auth.entity.Invitation;
import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.auth.exception.NoInvitationsLeftException;
import de.thi.mynd.auth.repository.InvitationRepository;
import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.utility.TokenGenerator;
import de.thi.mynd.notification.service.GenericEmailService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public final class InvitationServiceImpl implements InvitationService {

  @Inject InvitationRepository invitationRepository;

  @Inject MappingRegistry mappingRegistry;

  @Inject UserProfileService userProfileService;

  @Inject SecurityIdentity securityIdentity;

  @Inject
  GenericEmailService genericEmailService;

  @ConfigProperty(name = "mynd.frontendUri")
  String frontendUri;

  @Override
  public InvitationDto getInvitation(UUID invitationId) {
    Optional<Invitation> invitationOptional = invitationRepository.findByIdOptional(invitationId);

    if (invitationOptional.isEmpty()) {
      throw new EntityInstanceNotFoundException("There is no invitation with this ID");
    }

    return mappingRegistry.map(invitationOptional.get(), InvitationDto.class);
  }

  @Override
  public PaginationDto<InvitationDto> getSentInvitations(int page, int pageSize) {
    String creatorId = securityIdentity.getPrincipal().getName();
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

    return PersonalInvitationStatusDto.builder()
        .invitationsLeft(userProfile.invitationsLeft)
        .invitationsAlreadySent(alreadySent)
        .build();
  }

  @Override
  @Transactional
  public void sendInvitation(String email) {
    String creatorId = securityIdentity.getPrincipal().getName();
    UserProfile userProfile = getCurrentUsersProfile();

    if (userProfile.invitationsLeft == 0) {
      throw new NoInvitationsLeftException("You do not have any invitations left");
    }

    Invitation invitation = new Invitation();
    invitation.creatorId = creatorId;
    invitation.mailSentTo = email;
    invitation.redemptionSecret = TokenGenerator.generateRandomString(64);
    invitationRepository.persistAndFlush(invitation);

    sendInvitationEmail(invitation);
  }

  @Override
  public void redeemInvitation(UUID id, String secret) {
    // TODO: Implement this
  }

  private void sendInvitationEmail(Invitation invitation) {
    Map<String, String> parameters = Map.of(
            "logoUrl", frontendUri+"/mynd-logo.png",
            "invitationLink", String.format("%s/acceptInvite?id=%s&redemptionSecret=%s", frontendUri, invitation.id, invitation.redemptionSecret)
    );
    genericEmailService.sendEmail("invitation", "MYnd Invitation", List.of(invitation.mailSentTo), parameters);
  }

  private UserProfile getCurrentUsersProfile() {
    return
            userProfileService
                    .getPersonalUserProfile()
                    .orElse(userProfileService.createPersonalUserProfile());
  }
}
