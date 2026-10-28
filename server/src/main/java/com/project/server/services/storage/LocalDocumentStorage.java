package com.project.server.services.storage;

import com.project.server.exceptions.FileGenerationException;
import com.project.server.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Self-hosted/dev-friendly implementation: writes under a tenant-scoped subdirectory. The write
 * itself goes to a temp file in the same directory and is moved into place atomically, so a crash
 * mid-write never leaves a half-written file at the real path — unlike the cron's old direct
 * File/FileOutputStream writes.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "project.document-storage.type", havingValue = "local", matchIfMissing = true)
public class LocalDocumentStorage implements DocumentStorage {
    @Value("${project.document-storage.local.directory}")
    private String baseDirectory;

    @Override
    public void store(Long tenantId, String documentId, byte[] content) throws FileGenerationException {
        try {
            Path dir = Paths.get(baseDirectory, String.valueOf(tenantId));
            Files.createDirectories(dir);

            Path target = dir.resolve(documentId);
            Path temp = Files.createTempFile(dir, documentId, ".tmp");
            Files.write(temp, content);
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new FileGenerationException("Failed to store document: " + e.getMessage());
        }
    }

    @Override
    public byte[] retrieve(Long tenantId, String documentId) throws NotFoundException {
        Path target = Paths.get(baseDirectory, String.valueOf(tenantId), documentId);
        if (!Files.exists(target)) throw new NotFoundException("Document not found.");

        try {
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new NotFoundException("Document not found.");
        }
    }

    @Override
    public void delete(Long tenantId, String documentId) throws FileGenerationException {
        try {
            Files.deleteIfExists(Paths.get(baseDirectory, String.valueOf(tenantId), documentId));
        } catch (IOException e) {
            throw new FileGenerationException("Failed to delete document: " + e.getMessage());
        }
    }
}
