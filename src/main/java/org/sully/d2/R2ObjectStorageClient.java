package org.sully.d2;


import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class R2ObjectStorageClient {

    private R2Config r2Config;
    private S3Client s3Client;

    public static void main(String[] args) {
        File r2ConfigFile = new File(System.getenv().get("R2_CONFIG_PATH"));

        R2ObjectStorageClient r2Client = new R2ObjectStorageClient(r2ConfigFile);

        r2Client.upload("ITEM_SNAPSHOT", new File("C:\\Users\\sully\\D2LootSnapshots\\WEB_D2_ITEMS_20260315T034050866Z"),
                Map.of("SNAPSHOT_HOUR", "" + (System.currentTimeMillis() / (3_600_000))));
    }

    public R2ObjectStorageClient(File r2ConfigFile) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            this.r2Config = objectMapper.readValue(r2ConfigFile, R2Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                r2Config.getAccessKey(),
                r2Config.getSecretKey()
        );

        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .chunkedEncodingEnabled(false)
                .build();

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(r2Config.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto")) // Required by S3 SDK but not used by R2
                .serviceConfiguration(serviceConfiguration)
                .build();
    }

    public void upload(String objectName, File content, Map<String,String> objectMetadata) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(r2Config.getBucketName())
                        .metadata(objectMetadata)
                        .key(objectName)
                .build(), RequestBody.fromFile(content));
    }

}

