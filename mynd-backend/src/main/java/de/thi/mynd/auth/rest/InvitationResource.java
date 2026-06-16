/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.auth.rest;

import de.thi.mynd.auth.dto.InvitationDto;
import de.thi.mynd.auth.dto.PersonalInvitationStatusDto;
import de.thi.mynd.auth.dto.RedeemInvitationDto;
import de.thi.mynd.auth.dto.SendInvitationDto;
import de.thi.mynd.auth.service.InvitationService;
import de.thi.mynd.common.dto.PaginationDto;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/auth/invitations")
@Tag(name = "Invitations")
@SecurityRequirement(name = "keycloak")
public final class InvitationResource {

  @Inject InvitationService invitationService;

  @GET
  @Path("/{id}")
  @Operation(
      summary = "Get an invitation by ID",
      description = "Retrieves information about a specific invitation using its unique ID.")
  @APIResponse(responseCode = "200", description = "Invitation data found successfully")
  @APIResponse(responseCode = "404", description = "No invitation found with the given ID")
  public InvitationDto getInvitation(@PathParam("id") UUID invitationId) {
    return invitationService.getInvitation(invitationId);
  }

  @GET
  @RolesAllowed("authorizedUser")
  @Operation(
      summary = "Get sent invitations paginated",
      description =
          "Retrieves a paginated list of all invitations dispatched by the currently authenticated user.")
  @APIResponse(responseCode = "200", description = "Paginated list retrieved successfully")
  public PaginationDto<InvitationDto> getSentInvitations(
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
    return invitationService.getSentInvitations(page, pageSize);
  }

  @GET
  @Path("/status")
  @RolesAllowed("authorizedUser")
  @Operation(
      summary = "Get personal invitation token status",
      description =
          "Returns how many invitations the current user has left to send and how many they already processed.")
  @APIResponse(responseCode = "200", description = "Status metrics returned successfully")
  public PersonalInvitationStatusDto getPersonalInvitationStatus() {
    return invitationService.getPersonalInvitationStatus();
  }

  @POST
  @RolesAllowed("authorizedUser")
  @Operation(
      summary = "Send a new invitation link",
      description =
          "Deducts an allowance token from the user profile, creates an active invitation, and emails a dynamic signup link to the recipient.")
  @APIResponse(
      responseCode = "202",
      description = "Invitation payload recorded and dispatch pipeline triggered")
  @APIResponse(
      responseCode = "400",
      description = "User has exhausted their invitation limits allocation")
  public Response sendInvitation(@Valid SendInvitationDto invitationDto) {
    invitationService.sendInvitation(invitationDto.email);
    return Response.status(Response.Status.ACCEPTED).build();
  }

  @POST
  @Path("/{id}/redeem")
  @Operation(
      summary = "Redeem an active onboarding token",
      description =
          "Accepts an invitation, links it to the current user profile context, and provisions access roles.")
  @APIResponse(
      responseCode = "204",
      description = "Invitation successfully parsed and onboarding complete")
  @APIResponse(
      responseCode = "400",
      description = "Invalid redemption secret provided or user attempted to self-accept")
  @APIResponse(responseCode = "404", description = "Target invitation tracking key does not exist")
  public Response redeemInvitation(
      @PathParam("id") UUID id, @Valid RedeemInvitationDto redeemInvitationDto) {
    invitationService.redeemInvitation(id, redeemInvitationDto.secret);
    return Response.noContent().build();
  }
}
