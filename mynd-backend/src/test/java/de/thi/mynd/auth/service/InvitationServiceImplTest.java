package de.thi.mynd.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.auth.dto.InvitationDto;
import de.thi.mynd.auth.dto.PersonalInvitationStatusDto;
import de.thi.mynd.auth.entity.Invitation;
import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.auth.exception.CannotAcceptInvitationException;
import de.thi.mynd.auth.exception.NoInvitationsLeftException;
import de.thi.mynd.auth.repository.InvitationRepository;
import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.service.IdentityService;
import de.thi.mynd.notification.service.GenericEmailService;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.security.Principal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
class InvitationServiceImplTest {

  @Inject InvitationService invitationService;

  // InjectMock replaces normal beans with Mockito mocks within the Quarkus CDI context
  @InjectMock InvitationRepository invitationRepository;
  @InjectMock MappingRegistry mappingRegistry;
  @InjectMock UserProfileService userProfileService;
  @InjectMock SecurityIdentity securityIdentity;
  @InjectMock IdentityService identityService;
  @InjectMock GenericEmailService genericEmailService;

  private static final String CURRENT_USER_ID = "user-abc-123";
  private UserProfile mockProfile;
  private Principal mockPrincipal;

  @BeforeEach
  void setUp() {
    mockPrincipal = mock(Principal.class);
    when(securityIdentity.getPrincipal()).thenReturn(mockPrincipal);
    when(mockPrincipal.getName()).thenReturn(CURRENT_USER_ID);

    mockProfile = new UserProfile();
    mockProfile.invitationsLeft = 5;
    when(userProfileService.getPersonalUserProfile()).thenReturn(Optional.of(mockProfile));
  }

  @Test
  void testGetInvitation_Success() {
    UUID invitationId = UUID.randomUUID();
    Invitation invitation = new Invitation();
    invitation.id = invitationId;
    InvitationDto expectedDto = InvitationDto.builder().build();

    when(invitationRepository.findByIdOptional(invitationId)).thenReturn(Optional.of(invitation));
    when(mappingRegistry.map(invitation, InvitationDto.class)).thenReturn(expectedDto);

    InvitationDto result = invitationService.getInvitation(invitationId);

    assertNotNull(result);
    assertEquals(expectedDto, result);
  }

  @Test
  void testGetInvitation_NotFound_ThrowsException() {
    UUID invitationId = UUID.randomUUID();
    when(invitationRepository.findByIdOptional(invitationId)).thenReturn(Optional.empty());

    assertThrows(
        EntityInstanceNotFoundException.class, () -> invitationService.getInvitation(invitationId));
  }

  @Test
  void testGetPersonalInvitationStatus() {
    when(invitationRepository.getAmountInvitationsSubmittedPerUser(CURRENT_USER_ID)).thenReturn(3L);

    PersonalInvitationStatusDto status = invitationService.getPersonalInvitationStatus();

    assertEquals(5, status.invitationsLeft);
    assertEquals(3L, status.invitationsAlreadySent);
  }

  @Test
  void testSendInvitation_Success() {
    String targetEmail = "friend@example.com";

    // Capture arguments sent to the email engine to verify formatting correctness
    ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

    invitationService.sendInvitation(targetEmail);

    // Verify the database record got created and persisted
    verify(invitationRepository, times(1)).persistAndFlush(any(Invitation.class));

    // Verify notification pipeline triggered correctly
    verify(genericEmailService, times(1))
        .sendEmail(
            eq("invitation"),
            eq("MYnd Invitation"),
            eq(List.of(targetEmail)),
            paramsCaptor.capture());

    Map<String, String> capturedParams = paramsCaptor.getValue();
    assertTrue(capturedParams.containsKey("logoUrl"));
    assertTrue(capturedParams.get("invitationLink").contains("/acceptInvite?id="));
    assertTrue(capturedParams.get("invitationLink").contains("&redemptionSecret="));
  }

  @Test
  void testSendInvitation_NoInvitationsLeft_ThrowsException() {
    mockProfile.invitationsLeft = 0; // Empty invitations quota

    assertThrows(
        NoInvitationsLeftException.class, () -> invitationService.sendInvitation("test@test.com"));
    verify(invitationRepository, never()).persistAndFlush(any());
    verify(genericEmailService, never()).sendEmail(any(), any(), any(), any());
  }

  @Test
  void testRedeemInvitation_Success() {
    UUID invitationId = UUID.randomUUID();
    String correctSecret = "secret-token-xyz";

    Invitation invitation = new Invitation();
    invitation.id = invitationId;
    invitation.creatorId = "different-creator-id"; // Created by someone else
    invitation.redemptionSecret = correctSecret;

    when(invitationRepository.findByIdOptional(invitationId)).thenReturn(Optional.of(invitation));

    invitationService.redeemInvitation(invitationId, correctSecret);

    // Ensure invitation maps acceptor to current user
    assertEquals(CURRENT_USER_ID, invitation.acceptedBy);
    verify(invitationRepository, times(1)).persistAndFlush(invitation);

    // Ensure RBAC updates roles via Keycloak / Identity Engine
    verify(identityService, times(1)).addRolesToUser(CURRENT_USER_ID, List.of("authorizedUser"));
  }

  @Test
  void testRedeemInvitation_SelfAcceptance_ThrowsException() {
    UUID invitationId = UUID.randomUUID();
    Invitation invitation = new Invitation();
    invitation.id = invitationId;
    invitation.creatorId = CURRENT_USER_ID; // Creator trying to accept their own invitation

    when(invitationRepository.findByIdOptional(invitationId)).thenReturn(Optional.of(invitation));

    assertThrows(
        CannotAcceptInvitationException.class,
        () -> invitationService.redeemInvitation(invitationId, "any-secret"));
  }

  @Test
  void testRedeemInvitation_InvalidSecret_ThrowsException() {
    UUID invitationId = UUID.randomUUID();
    Invitation invitation = new Invitation();
    invitation.id = invitationId;
    invitation.creatorId = "external-user";
    invitation.redemptionSecret = "correct-secret";

    when(invitationRepository.findByIdOptional(invitationId)).thenReturn(Optional.of(invitation));

    assertThrows(
        CannotAcceptInvitationException.class,
        () -> invitationService.redeemInvitation(invitationId, "wrong-secret"));
  }
}
