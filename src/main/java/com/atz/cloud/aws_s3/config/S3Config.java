package com.atz.cloud.aws_s3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${aws.access.key}")
    private String awsAccessKey;
    @Value("${aws.secret.key}")
    private String awsSecretKey;
    @Value("${aws.region}")
    private String region;

    //Cliente sincrono
    @Bean
    public S3Client getS3Client() {
        AwsCredentials awsCredentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
        return S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create("http://s3." + region + ".amazonaws.com"))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    //Ejemplo cliente asincrono
    @Bean
    public S3AsyncClient getS3AsyncClient() {
        AwsCredentials awsCredentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
        return S3AsyncClient.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create("http://s3." + region + ".amazonaws.com"))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    @Bean //Se puede agregar (destroyMethod = "close") para librar recursos
    public S3Presigner getS3Presigner() { //Para firmar URLs S3
        AwsCredentials awsCredentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
        return S3Presigner.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create("http://s3." + region + ".amazonaws.com"))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

    }
}
