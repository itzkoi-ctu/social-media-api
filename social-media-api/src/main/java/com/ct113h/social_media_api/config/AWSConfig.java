package com.ct113h.social_media_api.config;

import com.mongodb.AwsCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AWSConfig {

    public static final String BUCKET_NAME = "social-media-app-ct113h";

    @Bean
    public S3Client s3Client(
            @Value("${aws.s3.accessKey}") String accessKey,
            @Value("${aws.s3.secretKey}") String secretKey
    ) {
        AwsBasicCredentials credentials = AwsBasicCredentials.builder()
                .accessKeyId(accessKey)
                .secretAccessKey(secretKey)
                .build();
        return S3Client.builder()
                .credentialsProvider(() -> credentials)
                .region(Region.AP_SOUTHEAST_2)
                .build();
    }
}