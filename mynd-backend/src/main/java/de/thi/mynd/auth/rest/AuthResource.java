package de.thi.mynd.auth.rest;

import de.thi.mynd.auth.dto.CheckUsernameExistsRequestDto;
import de.thi.mynd.auth.dto.RegisterUserRequestDto;
import de.thi.mynd.auth.exception.UserAlreadyExistsException;
import de.thi.mynd.auth.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/auth")
public final class AuthResource {

  @Inject AuthService authService;

  @POST
  @Path("/check-existance")
  public Response checkUserExists(@Valid CheckUsernameExistsRequestDto requestDto) {
    if (authService.checkUsernameExists(requestDto.username)) {
      return Response.ok().build();
    }
    return Response.noContent().build();
  }

  @POST
  @Path("/register")
  public Response createUserAccount(@Valid RegisterUserRequestDto requestDto) {

    try {
      authService.registerUser(requestDto);
      return Response.status(201).build();
    } catch (UserAlreadyExistsException e) {
      return Response.status(400).build();
    }
  }
}
