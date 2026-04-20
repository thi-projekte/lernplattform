package de.thi.mynd.common.service;

import de.thi.mynd.common.entity.BaseEntity;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.File;
import java.net.URL;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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

  @Inject S3Presigner presigner;

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
  public String uploadObject(BaseEntity entity, File file) {
    String objectKey = getS3FileName(entity, file.getName());
    uploadAsync(objectKey, file);

    return objectKey;
  }

  @Transactional(value = Transactional.TxType.NOT_SUPPORTED)
  public String uploadObject(BaseEntity entity, File file, String originalFileName) {
    String objectKey = getS3FileName(entity, originalFileName);
    uploadAsync(objectKey, file);

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

  private void uploadAsync(String objectKey, File file) {
    PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(objectKey).build();

    s3Client
        .putObject(request, file.toPath())
        .whenComplete(
            (response, exception) -> {
              if (exception != null) {
                Log.error(exception.getMessage());
              } else {
                Log.infof("Successfully uploaded object %s", objectKey);
              }
            });
  }

  private String getS3FileName(BaseEntity entity, String filename) {
    return String.format("%s/%s/%s", entity.getClass(), entity.id, filename).replaceAll(" ", "_");
  }
}
