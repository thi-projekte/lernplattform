package de.thi.mynd.auth.service;

import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.common.exception.UserNotFoundException;
import de.thi.mynd.common.service.IdentityService;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public final class AuthServiceImpl implements AuthService {

  @Inject IdentityService identityService;

  @Inject UserProfileService userProfileService;

  @ConfigProperty(name = "mynd.invitations.buidlerRegisterReward")
  int builderRegistrationReward;

  @Override
  public boolean checkUserIsBuilder(String username) {
    return identityService.getMyndRoles(username).stream()
        .anyMatch(r -> r.getName().equals("builder"));
  }

  @Override
  public void makeUserABuilder(String username) throws UserNotFoundException {
    List<String> newRoles = new ArrayList<>();
    newRoles.add("builder");
    identityService.addRolesToUser(username, newRoles);
    UserProfile userProfile = getUserProfileOfCurrentUser();
    userProfile.invitationsLeft += builderRegistrationReward;
    userProfileService.updateUserProfile(userProfile);

    Log.infof("Successfully made user %s a builder", username);
  }

  @Override
  public void makeUserALearner(String username) throws UserNotFoundException {
    List<String> newRoles = new ArrayList<>();
    newRoles.add("learner");
    identityService.addRolesToUser(username, newRoles);

    Log.infof("Successfully made user %s a learner", username);
  }

  private UserProfile getUserProfileOfCurrentUser() {
    return userProfileService.getPersonalUserProfile()
            .orElse(userProfileService.createPersonalUserProfile());
  }
}
