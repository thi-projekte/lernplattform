/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.auth.service;

import de.thi.mynd.auth.dto.ProfilePictureDto;
import de.thi.mynd.auth.entity.UserProfile;
import java.util.Optional;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public interface UserProfileService {
  ProfilePictureDto uploadProfilePicture(String username, FileUpload file);

  void deleteProfilePicture(String username);

  void updateUserProfile(UserProfile userProfile);

  ProfilePictureDto getProfilePicture(String username);

  UserProfile createPersonalUserProfile();

  Optional<UserProfile> getPersonalUserProfile();
}
