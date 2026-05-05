package de.thi.mynd.auth.service;

import de.thi.mynd.auth.RegisterRole;
import de.thi.mynd.auth.dto.RegisterUserRequestDto;
import de.thi.mynd.auth.exception.UserAlreadyExistsException;
import de.thi.mynd.common.service.IdentityService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

@ApplicationScoped
public final class AuthServiceImpl implements AuthService {

  @Inject IdentityService identityService;

  @Override
  public boolean checkUsernameExists(String username) {
    return identityService.userExists(username);
  }

  @Override
  public void registerUser(RegisterUserRequestDto requestDto) throws UserAlreadyExistsException {
    if (checkUsernameExists(requestDto.username)) {
      throw new UserAlreadyExistsException("This user already exists");
    }

    CredentialRepresentation creds = new CredentialRepresentation();
    creds.setType(CredentialRepresentation.PASSWORD);
    creds.setValue(requestDto.password);
    creds.setTemporary(false);

    UserRepresentation userRepresentation = new UserRepresentation();
    userRepresentation.setUsername(requestDto.username);
    userRepresentation.setEnabled(true);
    userRepresentation.setEmail(requestDto.email);
    userRepresentation.setFirstName(requestDto.firstName);
    userRepresentation.setLastName(requestDto.lastName);
    userRepresentation.setCredentials(List.of(creds));

    List<String> roles = new ArrayList<>();
    if (requestDto.role == RegisterRole.Builder) {
      roles.add("builder");
    }

    Map<String, List<String>> clientRoles = new HashMap<>();
    clientRoles.put("mynd", roles);

    userRepresentation.setClientRoles(clientRoles);

    identityService.createUser(userRepresentation);
  }
}
