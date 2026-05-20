package de.thi.mynd.auth.rest;

import de.thi.mynd.auth.dto.InvitationDto;
import de.thi.mynd.auth.dto.PersonalInvitationStatusDto;
import de.thi.mynd.auth.dto.RedeemInvitationDto;
import de.thi.mynd.auth.dto.SendInvitationDto;
import de.thi.mynd.auth.service.InvitationService;
import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@QuarkusTest
class InvitationResourceTest {

    @InjectMock
    InvitationService invitationService;

    @Test
    void testGetInvitation_AnonymousAllowed_Success() {
        UUID invitationId = UUID.randomUUID();
        InvitationDto expectedDto = InvitationDto.builder()
                .id(invitationId).build();

        when(invitationService.getInvitation(invitationId)).thenReturn(expectedDto);

        // No @TestSecurity annotation means this request is unauthenticated / public
        given()
                .pathParam("id", invitationId)
                .when()
                .get("/auth/invitations/{id}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(invitationId.toString()));

        verify(invitationService, times(1)).getInvitation(invitationId);
    }

    @Test
    void testGetInvitation_NotFound_Returns404() {
        UUID invitationId = UUID.randomUUID();

        // Ensure standard mapping exceptions are caught by your global exception mapper
        when(invitationService.getInvitation(invitationId))
                .thenThrow(new EntityInstanceNotFoundException("Not found"));

        given()
                .pathParam("id", invitationId)
                .when()
                .get("/auth/invitations/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void testGetSentInvitations_Unauthenticated_Returns401() {
        // Endpoint has @RolesAllowed, missing authorization should block immediately
        given()
                .queryParam("page", 0)
                .queryParam("pageSize", 5)
                .when()
                .get("/auth/invitations")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"authorizedUser"})
    void testGetSentInvitations_Authenticated_Success() {
        PaginationDto<InvitationDto> mockPagination = PaginationDto.<InvitationDto>builder()
                .results(List.of(InvitationDto.builder().build()))
                .totalPages(1)
                .build();

        when(invitationService.getSentInvitations(0, 5)).thenReturn(mockPagination);

        given()
                .queryParam("page", 0)
                .queryParam("pageSize", 5)
                .when()
                .get("/auth/invitations")
                .then()
                .statusCode(200)
                .body("totalPages", equalTo(1));

        verify(invitationService, times(1)).getSentInvitations(0, 5);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"authorizedUser"})
    void testGetPersonalInvitationStatus_Success() {
        PersonalInvitationStatusDto mockStatus = PersonalInvitationStatusDto.builder()
                .invitationsLeft(3)
                .invitationsAlreadySent(2L)
                .build();

        when(invitationService.getPersonalInvitationStatus()).thenReturn(mockStatus);

        given()
                .when()
                .get("/auth/invitations/status")
                .then()
                .statusCode(200)
                .body("invitationsLeft", equalTo(3))
                .body("invitationsAlreadySent", equalTo(2));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"authorizedUser"})
    void testSendInvitation_Success() {
        SendInvitationDto payload = new SendInvitationDto();
        payload.email = "newuser@mynd.com";

        doNothing().when(invitationService).sendInvitation(payload.email);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/auth/invitations")
                .then()
                .statusCode(202); // 202 ACCEPTED

        verify(invitationService, times(1)).sendInvitation(payload.email);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"authorizedUser"})
    void testSendInvitation_InvalidPayload_Returns400() {
        SendInvitationDto invalidPayload = new SendInvitationDto();
        invalidPayload.email = ""; // Triggers Jakarta validation constraints

        given()
                .contentType(ContentType.JSON)
                .body(invalidPayload)
                .when()
                .post("/auth/invitations")
                .then()
                .statusCode(400); // Bad Request from @Valid failure

        verifyNoInteractions(invitationService);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"authorizedUser"})
    void testRedeemInvitation_Success() {
        UUID invitationId = UUID.randomUUID();
        RedeemInvitationDto payload = new RedeemInvitationDto();
        payload.secret = "super-secret-token-123";

        doNothing().when(invitationService).redeemInvitation(invitationId, payload.secret);

        given()
                .pathParam("id", invitationId)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/auth/invitations/{id}/redeem")
                .then()
                .statusCode(204); // 204 No Content

        verify(invitationService, times(1)).redeemInvitation(invitationId, payload.secret);
    }
}