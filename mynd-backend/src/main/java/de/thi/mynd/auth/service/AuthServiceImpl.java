package de.thi.mynd.auth.service;

import de.thi.mynd.common.exception.UserNotFoundException;
import de.thi.mynd.common.service.IdentityService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public final class AuthServiceImpl implements AuthService {

  @Inject IdentityService identityService;

  @Override
  public boolean checkUserIsBuilder(String username) {
    String clientUuid = identityService.getMyndClientId();
    return identityService.getUser(username).getClientRoles().get(clientUuid).contains("builder");
  }

  @Override
  public void makeUserABuilder(String username) throws UserNotFoundException {
    if (checkUserIsBuilder(username)) {
      return;
    }
    List<String> newRoles = new ArrayList<>();
    newRoles.add("builder");
    identityService.addRolesToUser(username, newRoles);
  }

  @Override
  public void makeUserALearner(String username) throws UserNotFoundException {
    if (checkUserIsBuilder(username)) {
      return;
    }
    List<String> newRoles = new ArrayList<>();
    newRoles.add("learner");
    identityService.addRolesToUser(username, newRoles);
  }
}
