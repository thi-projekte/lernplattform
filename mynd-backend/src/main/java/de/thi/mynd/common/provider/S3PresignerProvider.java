package de.thi.mynd.common.provider;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@ApplicationScoped
public class S3PresignerProvider {

    @ConfigProperty(name = "mynd.s3.public-host")
    String externalEndpoint;

    @ConfigProperty(name = "quarkus.s3.aws.credentials.static-provider.access-key-id")
    String accessKey;

    @ConfigProperty(name = "quarkus.s3.aws.credentials.static-provider.secret-access-key")
    String secretKey;

    @Produces
    @ApplicationScoped
    @Named("external-presigner")
    public S3Presigner externalPresigner() {

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        S3Configuration configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        return S3Presigner.builder()
                .endpointOverride(URI.create(externalEndpoint))
                .region(Region.EU_CENTRAL_1)
                .serviceConfiguration(configuration)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
