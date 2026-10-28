package com.project.server.services.storage;

import com.project.server.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real integration test against a running S3-compatible backend (adobe/s3mock, started manually
 * via Docker on port 9100 before running this class — not managed by Maven). Proves the adapter
 * actually speaks S3 correctly, not just that it compiles against the AWS SDK's types.
 */
class RemoteDocumentStorageTest {
    private static final String ENDPOINT = "http://localhost:9100";
    private static final String BUCKET = "almanac-documents";

    private final DocumentStorage storage = new RemoteDocumentStorage(ENDPOINT, "us-east-1", "test", "test", BUCKET);

    @BeforeAll
    static void createBucket() {
        S3Client client = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .endpointOverride(URI.create(ENDPOINT))
                .forcePathStyle(true)
                .build();
        try {
            client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());
        } catch (BucketAlreadyOwnedByYouException ignored) {
            // already exists from a previous run against the same long-lived container
        }
    }

    @Test
    void storeThenRetrieve_returnsTheSameBytes() throws Exception {
        byte[] content = "a timetable document, stored remotely".getBytes(StandardCharsets.UTF_8);

        storage.store(1L, "remote-doc-1.docx", content);
        byte[] retrieved = storage.retrieve(1L, "remote-doc-1.docx");

        assertArrayEquals(content, retrieved);
    }

    @Test
    void retrieve_withNoSuchDocument_throwsNotFound() {
        assertThrows(NotFoundException.class, () -> storage.retrieve(1L, "does-not-exist.docx"));
    }

    @Test
    void differentTenants_withTheSameDocumentId_doNotCollide() throws Exception {
        storage.store(10L, "shared-name.docx", "tenant ten".getBytes(StandardCharsets.UTF_8));
        storage.store(20L, "shared-name.docx", "tenant twenty".getBytes(StandardCharsets.UTF_8));

        assertEquals("tenant ten", new String(storage.retrieve(10L, "shared-name.docx"), StandardCharsets.UTF_8));
        assertEquals("tenant twenty", new String(storage.retrieve(20L, "shared-name.docx"), StandardCharsets.UTF_8));
    }

    @Test
    void delete_removesTheDocument() throws Exception {
        storage.store(1L, "to-delete.docx", "content".getBytes(StandardCharsets.UTF_8));

        storage.delete(1L, "to-delete.docx");

        assertThrows(NotFoundException.class, () -> storage.retrieve(1L, "to-delete.docx"));
    }
}
