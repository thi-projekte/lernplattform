/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.common.service;

import de.thi.mynd.common.entity.BaseEntityWithId;
import de.thi.mynd.common.exception.NoFileProvidedException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ApplicationScoped
public final class S3ObjectStorageImpl implements ObjectStorageService {

  @ConfigProperty(name = "mynd.s3.bucket")
  String bucketName;

  @Inject
  @Named("external-presigner")
  S3Presigner presigner;

  @Inject S3AsyncClient s3Client;

  @Override
  public URL getPresignedUrlForFile(String objectKey) {
    GetObjectRequest objectRequest =
        GetObjectRequest.builder().bucket(bucketName).key(objectKey).build();

    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(5))
            .getObjectRequest(objectRequest)
            .build();

    PresignedGetObjectRequest finalRequest = presigner.presignGetObject(presignRequest);
    return finalRequest.url();
  }

  @Override
  @Transactional(value = Transactional.TxType.NOT_SUPPORTED)
  public String uploadObject(BaseEntityWithId entity, File file) {
    String objectKey = getS3FileName(entity, file.getName());
    byte[] bytes = getBytesFromFile(file);
    uploadAsync(objectKey, bytes);
    return objectKey;
  }

  @Transactional(value = Transactional.TxType.NOT_SUPPORTED)
  public String uploadObject(BaseEntityWithId entity, File file, String originalFileName) {
    String objectKey = getS3FileName(entity, originalFileName);

    byte[] bytes = getBytesFromFile(file);
    uploadAsync(objectKey, bytes);

    return objectKey;
  }

  @Override
  public String uploadObject(String objectKey, File file) {
    byte[] bytes = getBytesFromFile(file);
    uploadAsync(objectKey, bytes);

    return objectKey;
  }

  @Override
  public void tryDeleteObject(String objectKey) {
    DeleteObjectRequest request =
        DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build();

    s3Client
        .deleteObject(request)
        .whenComplete(
            (response, exception) -> {
              if (exception != null) {
                Log.error(exception.getMessage());
              } else {
                Log.infof("Successfully deleted object %s", objectKey);
              }
            });
  }

  private void uploadAsync(String objectKey, byte[] content) {
    PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(objectKey).build();

    s3Client
        .putObject(request, AsyncRequestBody.fromBytes(content))
        .whenComplete(
            (response, exception) -> {
              if (exception != null) {
                Log.error(exception.getMessage());
                Log.trace(exception);
              } else {
                Log.infof("Successfully uploaded object %s", objectKey);
              }
            });
  }

  private byte[] getBytesFromFile(File file) {
    try {
      return Files.readAllBytes(file.toPath());
    } catch (IOException e) {
      throw new NoFileProvidedException("Cannot read the bytes from uploaded file");
    }
  }

  private String getS3FileName(BaseEntityWithId entity, String filename) {
    return String.format("%s/%s/%s", entity.getClass(), entity.id, filename).replaceAll(" ", "_");
  }
}
