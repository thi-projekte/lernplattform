package de.thi.mynd.auth.service;

import de.thi.mynd.auth.dto.ProfilePictureDto;
import de.thi.mynd.auth.entity.UserProfile;
import de.thi.mynd.auth.repository.UserProfileRepository;
import de.thi.mynd.common.exception.FileTooLargeException;
import de.thi.mynd.common.exception.InvalidFileTypeException;
import de.thi.mynd.common.exception.NoFileProvidedException;
import de.thi.mynd.common.exception.ProfilePictureNotFoundException;
import de.thi.mynd.common.service.ObjectStorageService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        when(userProfileRepository.findByUsername("user1")).thenReturn(Optional.empty());
        when(objectStorageService.getPresignedUrlForFile(any())).thenReturn(URI.create("https://example.com/pic").toURL());

        ProfilePictureDto result = userProfileService.uploadProfilePicture("user1", mockFile);

        assertNotNull(result);
        verify(userProfileRepository).persistAndFlush(argThat(p -> p.creatorId.equals("user1")));
    }

    @Test
    void uploadProfilePicture_updatesExistingProfile_whenOneExists() throws MalformedURLException {
        UserProfile existing = new UserProfile();
        existing.creatorId = "user1";
        existing.profilePictureKey = "old/key";

        when(userProfileRepository.findByUsername("user1")).thenReturn(Optional.of(existing));
        when(objectStorageService.getPresignedUrlForFile(any())).thenReturn(URI.create("https://example.com/pic").toURL());

        userProfileService.uploadProfilePicture("user1", mockFile);

        verify(userProfileRepository).persistAndFlush(argThat(p -> p.profilePictureKey.equals("user_profile/user1/profile-picture")));
    }

    @Test
    void uploadProfilePicture_throwsNoFileProvidedException_whenFileIsNull() {
        assertThrows(NoFileProvidedException.class,
                () -> userProfileService.uploadProfilePicture("user1", null));
    }

    @Test
    void uploadProfilePicture_throwsInvalidFileTypeException_whenContentTypeIsNull() {
        when(mockFile.contentType()).thenReturn(null);

        assertThrows(InvalidFileTypeException.class,
                () -> userProfileService.uploadProfilePicture("user1", mockFile));
    }

    @Test
    void uploadProfilePicture_throwsInvalidFileTypeException_whenContentTypeIsNotImage() {
        when(mockFile.contentType()).thenReturn("application/pdf");

        assertThrows(InvalidFileTypeException.class,
                () -> userProfileService.uploadProfilePicture("user1", mockFile));
    }

    @Test
    void uploadProfilePicture_throwsFileTooLargeException_whenFilExceedsLimit() {
        when(mockFile.size()).thenReturn(6L * 1024 * 1024);

        assertThrows(FileTooLargeException.class,
                () -> userProfileService.uploadProfilePicture("user1", mockFile));
    }

    @Test
    void uploadProfilePicture_storesObjectWithCorrectKey() throws MalformedURLException {
        when(userProfileRepository.findByUsername("user1")).thenReturn(Optional.empty());
        when(objectStorageService.getPresignedUrlForFile(any())).thenReturn(URI.create("https://example.com/pic").toURL());

        userProfileService.uploadProfilePicture("user1", mockFile);

        verify(objectStorageService).uploadObject(eq("user_profile/user1/profile-picture"), any());
    }

    @Test
    void deleteProfilePicture_deletesObject_whenProfileHasPicture() {
        UserProfile profile = new UserProfile();
        profile.creatorId = "user1";
        profile.profilePictureKey = "user_profile/user1/profile-picture";

        when(userProfileRepository.findByUsername("user1")).thenReturn(Optional.of(profile));

        userProfileService.deleteProfilePicture("user1");

        verify(objectStorageService).tryDeleteObject("user_profile/user1/profile-picture");
        assertNull(profile.profilePictureKey);
    }

    @Test
    void deleteProfilePicture_throwsProfilePictureNotFoundException_whenNoProfile() {
        when(userProfileRepository.findByUsername("user1")).thenReturn(Optional.empty());

        assertThrows(ProfilePictureNotFoundException.class,
                () -> userProfileService.deleteProfilePicture("user1"));
    }

    @Test
    void deleteProfilePicture_throwsProfilePictureNotFoundException_whenPictureKeyIsNull() {
        UserProfile profile = new UserProfile();
        profile.creatorId = "user1";
        profile.profilePictureKey = null;

        when(userProfileRepository.findByUsername("user1")).thenReturn(Optional.of(profile));

        assertThrows(ProfilePictureNotFoundException.class,
                () -> userProfileService.deleteProfilePicture("user1"));
    }

    @Test
    void getProfilePicture_returnsPresignedUrl_whenProfileHasPicture() throws MalformedURLException {
        UserProfile profile = new UserProfile();
        profile.creatorId = "user1";
        profile.profilePictureKey = "user_profile/user1/profile-picture";

        when(userProfileRepository.findByUsername("user1")).thenReturn(Optional.of(profile));
        when(objectStorageService.getPresignedUrlForFile("user_profile/user1/profile-picture"))
                .thenReturn(URI.create("https://example.com/pic").toURL());

        ProfilePictureDto result = userProfileService.getProfilePicture("user1");

        assertEquals("https://example.com/pic", result.url());
    }

    @Test
    void getProfilePicture_throwsProfilePictureNotFoundException_whenNoProfile() {
        when(userProfileRepository.findByUsername("user1")).thenReturn(Optional.empty());

        assertThrows(ProfilePictureNotFoundException.class,
                () -> userProfileService.getProfilePicture("user1"));
    }

    @Test
    void getProfilePicture_throwsProfilePictureNotFoundException_whenPictureKeyIsNull() {
        UserProfile profile = new UserProfile();
        profile.creatorId = "user1";
        profile.profilePictureKey = null;

        when(userProfileRepository.findByUsername("user1")).thenReturn(Optional.of(profile));

        assertThrows(ProfilePictureNotFoundException.class,
                () -> userProfileService.getProfilePicture("user1"));
    }
}