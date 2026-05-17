package de.thi.mynd.auth.service;

import de.thi.mynd.auth.dto.ProfilePictureDto;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public interface UserProfileService {
  ProfilePictureDto uploadProfilePicture(String username, FileUpload file);

  void deleteProfilePicture(String username);

  ProfilePictureDto getProfilePicture(String username);
}
