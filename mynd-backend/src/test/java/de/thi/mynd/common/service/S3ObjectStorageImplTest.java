/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.thi.mynd.common.entity.BaseEntityWithId;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@QuarkusTest
class S3ObjectStorageImplTest {

  @Inject S3ObjectStorageImpl s3ObjectStorage;

  @InjectMock S3Presigner presigner;

  @InjectMock S3AsyncClient s3Client;

  private static final String BUCKET_NAME = "test-bucket";

  // Simple concrete implementation of BaseEntity for testing
  static class TestEntity extends BaseEntityWithId {
    public TestEntity(UUID id) {
      this.id = id;
    }
  }

  @BeforeEach
  void setup() {
    // Set the bucket name if it's not picked up from application.properties in test profile
    s3ObjectStorage.bucketName = BUCKET_NAME;
  }

  @Test
  void testGetPresignedUrlForFile() throws MalformedURLException {
    // Arrange
    String objectKey = "test-key.pdf";
    URL mockUrl = new URL("https://s3.amazonaws.com/test-bucket/test-key.pdf");
    PresignedGetObjectRequest mockPresignedRequest = mock(PresignedGetObjectRequest.class);

    when(mockPresignedRequest.url()).thenReturn(mockUrl);
    when(presigner.presignGetObject(any(GetObjectPresignRequest.class)))
        .thenReturn(mockPresignedRequest);

    // Act
    URL result = s3ObjectStorage.getPresignedUrlForFile(objectKey);

    // Assert
    assertNotNull(result);
    assertEquals(mockUrl, result);
    verify(presigner).presignGetObject(any(GetObjectPresignRequest.class));
  }

  @Test
  void testUploadObject() throws IOException {
    // Arrange
    UUID entityId = UUID.randomUUID();
    TestEntity entity = new TestEntity(entityId);
    File tempFile = File.createTempFile("test-upload", ".txt");
    tempFile.deleteOnExit();

    CompletableFuture<PutObjectResponse> future =
        CompletableFuture.completedFuture(PutObjectResponse.builder().build());
    when(s3Client.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenReturn(future);

    // Act
    String resultKey = s3ObjectStorage.uploadObject(entity, tempFile);

    // Assert
    assertNotNull(resultKey);
    assertTrue(resultKey.contains(entityId.toString()));
    assertTrue(resultKey.contains("test-upload"));
    verify(s3Client).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
  }

  @Test
  void testUploadObjectWithCustomName() throws IOException {
    // Arrange
    UUID entityId = UUID.randomUUID();
    TestEntity entity = new TestEntity(entityId);
    File tempFile = File.createTempFile("test-upload", ".txt");
    tempFile.deleteOnExit();

    CompletableFuture<PutObjectResponse> future =
        CompletableFuture.completedFuture(PutObjectResponse.builder().build());
    when(s3Client.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenReturn(future);

    // Act
    String resultKey = s3ObjectStorage.uploadObject(entity, tempFile, "custom-file-name");

    // Assert
    assertNotNull(resultKey);
    assertTrue(resultKey.contains(entityId.toString()));
    assertTrue(resultKey.contains("custom-file-name"));
    verify(s3Client).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
  }

  @Test
  void testUploadObjectWithCustomKey() throws IOException {
    // Arrange
    File tempFile = File.createTempFile("test-upload", ".txt");
    tempFile.deleteOnExit();
    String key = "custom-key";

    CompletableFuture<PutObjectResponse> future =
        CompletableFuture.completedFuture(PutObjectResponse.builder().build());
    when(s3Client.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenReturn(future);

    // Act
    String resultKey = s3ObjectStorage.uploadObject(key, tempFile);

    // Assert
    assertNotNull(resultKey);
    assertTrue(resultKey.equals(key));
    verify(s3Client).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
  }

  @Test
  void testTryDeleteObject() {
    // Arrange
    String objectKey = "delete-me.png";
    s3ObjectStorage.bucketName = "default";

    CompletableFuture<DeleteObjectResponse> future =
        CompletableFuture.completedFuture(DeleteObjectResponse.builder().build());

    when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(future);

    // Act
    s3ObjectStorage.tryDeleteObject(objectKey);

    // Assert
    verify(s3Client)
        .deleteObject(
            argThat(
                (DeleteObjectRequest req) ->
                    req.bucket().equals("default") && req.key().equals(objectKey)));
  }
}
