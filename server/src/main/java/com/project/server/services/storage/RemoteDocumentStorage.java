package com.project.server.services.storage;

import com.project.server.exceptions.FileGenerationException;
import com.project.server.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;

/**
 * The default for hosted multi-tenant production — any S3-compatible bucket (AWS S3, Cloudflare
 * R2, self-hosted MinIO). Which provider it points at is a config value (endpoint override), not
 * a code choice — see Almanac Blueprint §03.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "project.document-storage.type", havingValue = "remote")
public class RemoteDocumentStorage implements DocumentStorage {
    private final S3Client s3Client;
    private final String bucket;

    public RemoteDocumentStorage(
            @Value("${project.document-storage.remote.endpoint:}") String endpoint,
            @Value("${project.document-storage.remote.region:us-east-1}") String region,
            @Value("${project.document-storage.remote.access-key:}") String accessKey,
            @Value("${project.document-storage.remote.secret-key:}") String secretKey,
            @Value("${project.document-storage.remote.bucket}") String bucket
    ) {
        this.bucket = bucket;

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));

        if (endpoint != null && !endpoint.isBlank()) {
            // path-style addressing is required by MinIO/S3Mock and most non-AWS S3-compatible
            // backends — virtual-hosted-style (bucket.endpoint/...) generally only works against
            // real AWS S3.
            builder.endpointOverride(URI.create(endpoint)).forcePathStyle(true);
        }

        this.s3Client = builder.build();
    }

    private String key(Long tenantId, String documentId) {
        return tenantId + "/" + documentId;
    }

    @Override
    public void store(Long tenantId, String documentId, byte[] content) throws FileGenerationException {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(key(tenantId, documentId)).build(),
                    RequestBody.fromBytes(content)
            );
        } catch (Exception e) {
            throw new FileGenerationException("Failed to store document remotely: " + e.getMessage());
        }
    }

    @Override
    public byte[] retrieve(Long tenantId, String documentId) throws NotFoundException {
        try {
            return s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(key(tenantId, documentId)).build()).readAllBytes();
        } catch (NoSuchKeyException e) {
            throw new NotFoundException("Document not found.");
        } catch (IOException e) {
            throw new NotFoundException("Document not found.");
        }
    }

    @Override
    public void delete(Long tenantId, String documentId) throws FileGenerationException {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key(tenantId, documentId)).build());
        } catch (Exception e) {
            throw new FileGenerationException("Failed to delete document remotely: " + e.getMessage());
        }
    }
}
