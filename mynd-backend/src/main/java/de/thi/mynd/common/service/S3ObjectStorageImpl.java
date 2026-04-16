package de.thi.mynd.common.service;

import de.thi.mynd.common.entity.BaseEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

@ApplicationScoped
public final class S3ObjectStorageImpl implements ObjectStorageService {

    @ConfigProperty(name = "mynd.s3.bucket")
    String bucketName;

    @Inject
    S3Presigner presigner;

    @Override
    public URL getPresignedUrlForEntityFile(BaseEntity entity, String filename) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(getS3FileName(entity, filename))
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest finalRequest = presigner.presignGetObject(presignRequest);
        return finalRequest.url();
    }

    @Override
    public URL getPresignedCreationUrlForEntityFile(BaseEntity entity, String filename, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(getS3FileName(entity, filename))
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(request)
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
        return presignedRequest.url();
    }

    private String getS3FileName(BaseEntity entity, String filename) {
        return String.format("%s/%s/%s", entity.getClass(), entity.id, filename);
    }
}
