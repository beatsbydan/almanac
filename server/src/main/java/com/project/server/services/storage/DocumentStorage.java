package com.project.server.services.storage;

import com.project.server.exceptions.FileGenerationException;
import com.project.server.exceptions.NotFoundException;

/**
 * A single storage contract behind two implementations (local filesystem, S3-compatible remote
 * bucket), picked by configuration rather than code — see Almanac Blueprint §03/§06. Documents
 * are keyed by tenant and a caller-chosen id, written once as a completed artifact; neither
 * implementation supports partial/incremental writes, which is deliberate — that's what retires
 * the old "resume writing into the same file on disk" design with no way to tell a half-written
 * file from a finished one after a crash.
 */
public interface DocumentStorage {
    void store(Long tenantId, String documentId, byte[] content) throws FileGenerationException;
    byte[] retrieve(Long tenantId, String documentId) throws NotFoundException;
    void delete(Long tenantId, String documentId) throws FileGenerationException;
}
