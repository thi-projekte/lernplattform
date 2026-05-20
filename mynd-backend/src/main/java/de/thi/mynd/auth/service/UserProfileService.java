package de.thi.mynd.auth.service;

import de.thi.mynd.auth.dto.ProfilePictureDto;
import de.thi.mynd.auth.entity.UserProfile;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.util.Optional;

public interface UserProfileService {
  ProfilePictureDto uploadProfilePicture(String username, FileUpload file);

  void deleteProfilePicture(String username);

  ProfilePictureDto getProfilePicture(String username);

  UserProfile createPersonalUserProfile();

  Optional<UserProfile> getPersonalUserProfile();
}
