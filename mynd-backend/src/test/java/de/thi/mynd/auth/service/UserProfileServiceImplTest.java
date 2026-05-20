package de.thi.mynd.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.thi.mynd.auth.dto.ProfilePictureDto;
import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.auth.exception.ProfilePictureNotFoundException;
import de.thi.mynd.auth.repository.UserProfileRepository;
import de.thi.mynd.common.exception.FileTooLargeException;
import de.thi.mynd.common.exception.InvalidFileTypeException;
import de.thi.mynd.common.exception.NoFileProvidedException;
import de.thi.mynd.common.service.ObjectStorageService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
class UserProfileServiceImplTest {

  @Inject UserProfileService userProfileService;

  @InjectMock UserProfileRepository userProfileRepository;

  @InjectMock ObjectStorageService objectStorageService;

  private FileUpload mockFile;

  @BeforeEach
  void setUp() {
    mockFile = mock(FileUpload.class);
    when(mockFile.contentType()).thenReturn("image/jpeg");
    when(mockFile.size()).thenReturn(1024L);
    when(mockFile.uploadedFile()).thenReturn(Path.of("/tmp/test-image.jpg"));
  }

  @Test
  void uploadProfilePicture_createsNewProfile_whenNoneExists() throws MalformedURLException {
    when(userProfileRepository.findByUsernameOptional("user1")).thenReturn(Optional.empty());
    when(objectStorageService.getPresignedUrlForFile(any()))
        .thenReturn(URI.create("https://example.com/pic").toURL());

    ProfilePictureDto result = userProfileService.uploadProfilePicture("user1", mockFile);

    assertNotNull(result);
    verify(userProfileRepository).persistAndFlush(argThat(p -> p.creatorId.equals("user1")));
  }

  @Test
  void uploadProfilePicture_updatesExistingProfile_whenOneExists() throws MalformedURLException {
    UserProfile existing = new UserProfile();
    existing.creatorId = "user1";
    existing.profilePictureKey = "old/key";

    when(userProfileRepository.findByUsernameOptional("user1")).thenReturn(Optional.of(existing));
    when(objectStorageService.getPresignedUrlForFile(any()))
        .thenReturn(URI.create("https://example.com/pic").toURL());

    userProfileService.uploadProfilePicture("user1", mockFile);

    verify(userProfileRepository)
        .persistAndFlush(
            argThat(p -> p.profilePictureKey.equals("user_profile/user1/profile-picture")));
  }

  @Test
  void uploadProfilePicture_throwsNoFileProvidedException_whenFileIsNull() {
    assertThrows(
        NoFileProvidedException.class,
        () -> userProfileService.uploadProfilePicture("user1", null));
  }

  @Test
  void uploadProfilePicture_throwsInvalidFileTypeException_whenContentTypeIsNull() {
    when(mockFile.contentType()).thenReturn(null);

    assertThrows(
        InvalidFileTypeException.class,
        () -> userProfileService.uploadProfilePicture("user1", mockFile));
  }

  @Test
  void uploadProfilePicture_throwsInvalidFileTypeException_whenContentTypeIsNotImage() {
    when(mockFile.contentType()).thenReturn("application/pdf");

    assertThrows(
        InvalidFileTypeException.class,
        () -> userProfileService.uploadProfilePicture("user1", mockFile));
  }

  @Test
  void uploadProfilePicture_throwsFileTooLargeException_whenFilExceedsLimit() {
    when(mockFile.size()).thenReturn(6L * 1024 * 1024);

    assertThrows(
        FileTooLargeException.class,
        () -> userProfileService.uploadProfilePicture("user1", mockFile));
  }

  @Test
  void uploadProfilePicture_storesObjectWithCorrectKey() throws MalformedURLException {
    when(userProfileRepository.findByUsernameOptional("user1")).thenReturn(Optional.empty());
    when(objectStorageService.getPresignedUrlForFile(any()))
        .thenReturn(URI.create("https://example.com/pic").toURL());

    userProfileService.uploadProfilePicture("user1", mockFile);

    verify(objectStorageService).uploadObject(eq("user_profile/user1/profile-picture"), any());
  }

  @Test
  void deleteProfilePicture_deletesObject_whenProfileHasPicture() {
    UserProfile profile = new UserProfile();
    profile.creatorId = "user1";
    profile.profilePictureKey = "user_profile/user1/profile-picture";

    when(userProfileRepository.findByUsernameOptional("user1")).thenReturn(Optional.of(profile));

    userProfileService.deleteProfilePicture("user1");

    verify(objectStorageService).tryDeleteObject("user_profile/user1/profile-picture");
    assertNull(profile.profilePictureKey);
  }

  @Test
  void deleteProfilePicture_throwsProfilePictureNotFoundException_whenNoProfile() {
    when(userProfileRepository.findByUsernameOptional("user1")).thenReturn(Optional.empty());

    assertThrows(
        ProfilePictureNotFoundException.class,
        () -> userProfileService.deleteProfilePicture("user1"));
  }

  @Test
  void deleteProfilePicture_throwsProfilePictureNotFoundException_whenPictureKeyIsNull() {
    UserProfile profile = new UserProfile();
    profile.creatorId = "user1";
    profile.profilePictureKey = null;

    when(userProfileRepository.findByUsernameOptional("user1")).thenReturn(Optional.of(profile));

    assertThrows(
        ProfilePictureNotFoundException.class,
        () -> userProfileService.deleteProfilePicture("user1"));
  }

  @Test
  void getProfilePicture_returnsPresignedUrl_whenProfileHasPicture() throws MalformedURLException {
    UserProfile profile = new UserProfile();
    profile.creatorId = "user1";
    profile.profilePictureKey = "user_profile/user1/profile-picture";

    when(userProfileRepository.findByUsernameOptional("user1")).thenReturn(Optional.of(profile));
    when(objectStorageService.getPresignedUrlForFile("user_profile/user1/profile-picture"))
        .thenReturn(URI.create("https://example.com/pic").toURL());

    ProfilePictureDto result = userProfileService.getProfilePicture("user1");

    assertEquals("https://example.com/pic", result.url());
  }

  @Test
  void getProfilePicture_throwsProfilePictureNotFoundException_whenNoProfile() {
    when(userProfileRepository.findByUsernameOptional("user1")).thenReturn(Optional.empty());

    assertThrows(
        ProfilePictureNotFoundException.class, () -> userProfileService.getProfilePicture("user1"));
  }

  @Test
  void getProfilePicture_throwsProfilePictureNotFoundException_whenPictureKeyIsNull() {
    UserProfile profile = new UserProfile();
    profile.creatorId = "user1";
    profile.profilePictureKey = null;

    when(userProfileRepository.findByUsernameOptional("user1")).thenReturn(Optional.of(profile));

    assertThrows(
        ProfilePictureNotFoundException.class, () -> userProfileService.getProfilePicture("user1"));
  }

  @Test
  @TestSecurity(user = "user-abc-123")
  void shouldCreatePersonalUserProfileSuccessfully() {
    // Arrange: Simulate that no profile exists yet for this user
    when(userProfileRepository.findByUsernameOptional("user-abc-123"))
            .thenReturn(Optional.empty());

    // Act
    UserProfile result = userProfileService.createPersonalUserProfile();

    // Assert
    assertNotNull(result);
    assertEquals("user-abc-123", result.creatorId);
    assertNotNull(result.id);
    assertEquals("user-abc-123", result.id.creatorId);

    // Verify it was correctly flushed down to database storage layer
    ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
    verify(userProfileRepository, times(1)).persistAndFlush(profileCaptor.capture());

    UserProfile savedProfile = profileCaptor.getValue();
    assertEquals("user-abc-123", savedProfile.creatorId);
  }

  @Test
  @TestSecurity(user = "user-abc-123")
  void shouldThrowExceptionWhenProfileAlreadyExists() {
    // Arrange: Simulate an existing profile footprint
    UserProfile existingProfile = new UserProfile();
    when(userProfileRepository.findByUsernameOptional("user-abc-123"))
            .thenReturn(Optional.of(existingProfile));

    // Act & Assert
    // (Replace with your exact package exception if it's custom)
    Exception exception = assertThrows(RuntimeException.class, () -> {
      userProfileService.createPersonalUserProfile();
    });

    assertTrue(exception.getMessage().contains("You already have a user profile"));

    // Verify we blocked database corruption and never triggered persist mechanics
    verify(userProfileRepository, never()).persistAndFlush(any(UserProfile.class));
  }

  @Test
  @TestSecurity(user = "user-abc-123")
  void shouldReturnProfileWhenGetPersonalUserProfileIsCalled() {
    // Arrange
    UserProfile expectedProfile = new UserProfile();
    expectedProfile.creatorId = "user-abc-123";

    when(userProfileRepository.findByUsernameOptional("user-abc-123"))
            .thenReturn(Optional.of(expectedProfile));

    // Act
    Optional<UserProfile> result = userProfileService.getPersonalUserProfile();

    // Assert
    assertTrue(result.isPresent());
    assertEquals("user-abc-123", result.get().creatorId);
    verify(userProfileRepository, times(1)).findByUsernameOptional("user-abc-123");
  }

  @Test
  @TestSecurity(user = "user-abc-123")
  void shouldReturnEmptyOptionalWhenNoProfileExists() {
    // Arrange
    when(userProfileRepository.findByUsernameOptional("user-abc-123"))
            .thenReturn(Optional.empty());

    // Act
    Optional<UserProfile> result = userProfileService.getPersonalUserProfile();

    // Assert
    assertTrue(result.isEmpty());
    verify(userProfileRepository, times(1)).findByUsernameOptional("user-abc-123");
  }
}
