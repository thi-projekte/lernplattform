package de.thi.mynd.auth.service;

import de.thi.mynd.auth.dto.ProfilePictureDto;
import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.auth.repository.UserProfileRepository;
import de.thi.mynd.common.service.ObjectStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import de.thi.mynd.common.exception.ProfilePictureNotFoundException;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
public final class UserProfileServiceImpl implements UserProfileService {

  @Inject UserProfileRepository userProfileRepository;

  @Inject ObjectStorageService objectStorageService;

  @Override
  @Transactional
  public ProfilePictureDto uploadProfilePicture(String username, FileUpload file) {
    var existing = userProfileRepository.findByUsername(username);
    UserProfile profile =
        existing.orElseGet(
            () -> {
              UserProfile newProfile = new UserProfile();
              newProfile.creatorId = username;
              return newProfile;
            });

    String objectKey = "user_profile/" + username + "/profile-picture";
    objectStorageService.uploadObject(objectKey, file.uploadedFile().toFile());
    profile.profilePictureKey = objectKey;

    if (existing.isEmpty()) {
      userProfileRepository.persistAndFlush(profile);
    }

    return new ProfilePictureDto(
        objectStorageService.getPresignedUrlForFile(objectKey).toString());
  }

  @Override
  @Transactional
  public void deleteProfilePicture(String username) {
    UserProfile profile =
        userProfileRepository
            .findByUsername(username)
            .filter(p -> p.profilePictureKey != null)
            .orElseThrow(() -> new ProfilePictureNotFoundException("No profile picture found for user"));

    objectStorageService.tryDeleteObject(profile.profilePictureKey);
    profile.profilePictureKey = null;
  }

  @Override
  public ProfilePictureDto getProfilePicture(String username) {
    UserProfile profile =
        userProfileRepository
            .findByUsername(username)
            .filter(p -> p.profilePictureKey != null)
            .orElseThrow(() -> new ProfilePictureNotFoundException("No profile picture found for user"));

    return new ProfilePictureDto(
        objectStorageService.getPresignedUrlForFile(profile.profilePictureKey).toString());
  }
}