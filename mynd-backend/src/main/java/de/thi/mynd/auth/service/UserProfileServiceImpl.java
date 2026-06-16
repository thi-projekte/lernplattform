/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.auth.service;

import de.thi.mynd.auth.dto.ProfilePictureDto;
import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.auth.exception.ProfilePictureNotFoundException;
import de.thi.mynd.auth.repository.UserProfileRepository;
import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.exception.FileTooLargeException;
import de.thi.mynd.common.exception.InvalidFileTypeException;
import de.thi.mynd.common.exception.NoFileProvidedException;
import de.thi.mynd.common.service.ObjectStorageService;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
public final class UserProfileServiceImpl implements UserProfileService {

  private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

  @Inject UserProfileRepository userProfileRepository;

  @Inject ObjectStorageService objectStorageService;

  @Inject SecurityIdentity securityIdentity;

  @Override
  @Transactional
  public ProfilePictureDto uploadProfilePicture(String username, FileUpload file) {

    validateFile(file);

    var existing = userProfileRepository.findByUsernameOptional(username);
    UserProfile profile =
        existing.orElseGet(
            () -> {
              UserProfile newProfile = new UserProfile();
              newProfile.creatorId = username;
              CreatorIdKey id = new CreatorIdKey();
              id.creatorId = username;
              newProfile.id = id;
              return newProfile;
            });

    String objectKey = "user_profile/" + username + "/profile-picture";
    objectStorageService.uploadObject(objectKey, file.uploadedFile().toFile());
    profile.profilePictureKey = objectKey;

    userProfileRepository.persistAndFlush(profile);

    Log.infof("User %s successfully uploaded new profile picture", username);

    return new ProfilePictureDto(objectStorageService.getPresignedUrlForFile(objectKey).toString());
  }

  @Override
  @Transactional
  public void deleteProfilePicture(String username) {
    UserProfile profile =
        userProfileRepository
            .findByUsernameOptional(username)
            .filter(p -> p.profilePictureKey != null)
            .orElseThrow(
                () -> new ProfilePictureNotFoundException("No profile picture found for user"));

    objectStorageService.tryDeleteObject(profile.profilePictureKey);
    profile.profilePictureKey = null;
    userProfileRepository.persistAndFlush(profile);

    Log.infof("User % deleted his profile picture", username);
  }

  @Override
  @Transactional
  public void updateUserProfile(UserProfile userProfile) {
    userProfileRepository.getEntityManager().merge(userProfile);
    userProfileRepository.flush();

    Log.tracef("Updated user profile for %s", userProfile.creatorId);
  }

  @Override
  public ProfilePictureDto getProfilePicture(String username) {
    UserProfile profile =
        userProfileRepository
            .findByUsernameOptional(username)
            .filter(p -> p.profilePictureKey != null)
            .orElseThrow(
                () -> new ProfilePictureNotFoundException("No profile picture found for user"));

    Log.tracef("Fetching user profile picture for %", username);

    return new ProfilePictureDto(
        objectStorageService.getPresignedUrlForFile(profile.profilePictureKey).toString());
  }

  @Override
  @Transactional
  public UserProfile createPersonalUserProfile() {
    String userId = securityIdentity.getPrincipal().getName();

    if (getPersonalUserProfile().isPresent()) {
      throw new EntityInstanceNotFoundException("You already have a user profile");
    }

    UserProfile newProfile = new UserProfile();
    newProfile.creatorId = userId;
    CreatorIdKey id = new CreatorIdKey();
    id.creatorId = userId;
    newProfile.id = id;

    userProfileRepository.persistAndFlush(newProfile);

    Log.infof("Created personal user profile for user %s", userId);

    return newProfile;
  }

  @Override
  public Optional<UserProfile> getPersonalUserProfile() {
    String userId = securityIdentity.getPrincipal().getName();
    Log.tracef("Getting personal user profile for user %s", userId);
    return userProfileRepository.findByUsernameOptional(userId);
  }

  private void validateFile(FileUpload file) {
    if (file == null) {
      throw new NoFileProvidedException("Image file is missing");
    }

    if (!isFileTypeValid(file)) {
      throw new InvalidFileTypeException("The file is not a valid image");
    }

    if (file.size() > MAX_FILE_SIZE_BYTES) {
      throw new FileTooLargeException("Image file must not exceed 10 MB");
    }
  }

  private boolean isFileTypeValid(FileUpload file) {
    String contentType = file.contentType();
    if (contentType == null) {
      return false;
    }
    return contentType.startsWith("image/");
  }
}
