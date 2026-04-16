package de.thi.mynd.common.service;

import de.thi.mynd.common.entity.BaseEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;

@ApplicationScoped
public final class S3ObjectStorageImpl implements ObjectStorageService {

    @ConfigProperty(name = "mynd.s3.bucket")
    String bucketName;

    @Inject
    S3Presigner presigner;

    @Inject
    S3Client s3Client;

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
    public String uploadObject(BaseEntity entity, File file) throws IOException {
        String objectKey = getS3FileName(entity, file.getName());
        String contentType = Files.probeContentType(file.toPath());

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, file.toPath());

        return objectKey;
    }

    private String getS3FileName(BaseEntity entity, String filename) {
        return String.format("%s/%s/%s", entity.getClass(), entity.id, filename);
    }
}
