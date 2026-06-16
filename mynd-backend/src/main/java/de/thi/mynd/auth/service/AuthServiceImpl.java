/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.auth.service;

import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.common.exception.UserNotFoundException;
import de.thi.mynd.common.service.IdentityService;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public final class AuthServiceImpl implements AuthService {

  @Inject IdentityService identityService;

  @Inject UserProfileService userProfileService;

  @ConfigProperty(name = "mynd.invitations.buidlerRegisterReward")
  int builderRegistrationReward;

  @ConfigProperty(name = "mynd.invitations.learnerRegisterReward")
  int learnerRegistrationReward;

  @Override
  public boolean checkUserIsBuilder(String username) {

    Log.tracef("Checking if user %s is a builder", username);

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

    UserProfile userProfile = getUserProfileOfCurrentUser();
    userProfile.invitationsLeft += learnerRegistrationReward;
    userProfileService.updateUserProfile(userProfile);

    Log.infof("Successfully made user %s a learner", username);
  }

  private UserProfile getUserProfileOfCurrentUser() {

    Log.tracef("Fetching user profile of current user");
    return userProfileService
        .getPersonalUserProfile()
        .orElseGet(() -> userProfileService.createPersonalUserProfile());
  }
}
