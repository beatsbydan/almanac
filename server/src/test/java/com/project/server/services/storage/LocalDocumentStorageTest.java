package com.project.server.services.storage;

import com.project.server.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalDocumentStorageTest {
    @TempDir
    Path tempDir;

    private LocalDocumentStorage storage;

    @BeforeEach
    void setUp() throws Exception {
        storage = new LocalDocumentStorage();
        Field field = LocalDocumentStorage.class.getDeclaredField("baseDirectory");
        field.setAccessible(true);
        field.set(storage, tempDir.toString());
    }

    @Test
    void storeThenRetrieve_returnsTheSameBytes() throws Exception {
        byte[] content = "a timetable document".getBytes(StandardCharsets.UTF_8);

        storage.store(1L, "doc-1.docx", content);
        byte[] retrieved = storage.retrieve(1L, "doc-1.docx");

        assertArrayEquals(content, retrieved);
    }

    @Test
    void retrieve_withNoSuchDocument_throwsNotFound() {
        assertThrows(NotFoundException.class, () -> storage.retrieve(1L, "missing.docx"));
    }

    @Test
    void differentTenants_withTheSameDocumentId_doNotCollide() throws Exception {
        storage.store(1L, "doc.docx", "tenant one".getBytes(StandardCharsets.UTF_8));
        storage.store(2L, "doc.docx", "tenant two".getBytes(StandardCharsets.UTF_8));

        assertEquals("tenant one", new String(storage.retrieve(1L, "doc.docx"), StandardCharsets.UTF_8));
        assertEquals("tenant two", new String(storage.retrieve(2L, "doc.docx"), StandardCharsets.UTF_8));
    }

    @Test
    void delete_removesTheDocument() throws Exception {
        storage.store(1L, "doc.docx", "content".getBytes(StandardCharsets.UTF_8));

        storage.delete(1L, "doc.docx");

        assertThrows(NotFoundException.class, () -> storage.retrieve(1L, "doc.docx"));
    }

    @Test
    void store_overwritesAPreviousVersionAtomically() throws Exception {
        storage.store(1L, "doc.docx", "version 1".getBytes(StandardCharsets.UTF_8));
        storage.store(1L, "doc.docx", "version 2".getBytes(StandardCharsets.UTF_8));

        assertEquals("version 2", new String(storage.retrieve(1L, "doc.docx"), StandardCharsets.UTF_8));
    }
}
