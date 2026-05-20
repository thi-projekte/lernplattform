package de.thi.mynd.auth.service;

import de.thi.mynd.auth.dto.InvitationDto;
import de.thi.mynd.auth.dto.PersonalInvitationStatusDto;
import de.thi.mynd.auth.entity.Invitation;
import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.auth.repository.InvitationRepository;
import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.processor.MappingRegistry;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public final class InvitationServiceImpl implements InvitationService {

    @Inject
    InvitationRepository invitationRepository;

    @Inject
    MappingRegistry mappingRegistry;

    @Inject UserProfileService userProfileService;

    @Inject
    SecurityIdentity securityIdentity;

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
        PaginationDto<Invitation> invitationPagination = invitationRepository.getInvitationsForCreatorPaginated(creatorId, page, pageSize);

        return PaginationDto.<InvitationDto>builder()
                .results(mappingRegistry.mapList(invitationPagination.results, InvitationDto.class))
                .totalPages(invitationPagination.totalPages)
                .build();
    }

    @Override
    public PersonalInvitationStatusDto getPersonalInvitationStatus() {
        String creatorId = securityIdentity.getPrincipal().getName();
        long alreadySent = invitationRepository.getAmountInvitationsSubmittedPerUser(creatorId);
        UserProfile userProfile = userProfileService.getPersonalUserProfile().orElse(userProfileService.createPersonalUserProfile());

        return PersonalInvitationStatusDto.builder()
                .invitationsLeft(userProfile.invitationsLeft)
                .invitationsAlreadySent(alreadySent)
                .build();
    }

    @Override
    public void sendInvitation(String email) {

    }

    @Override
    public void redeemInvitation(UUID id, String secret) {

    }


}
