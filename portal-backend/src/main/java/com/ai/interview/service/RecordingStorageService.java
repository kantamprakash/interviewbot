package com.ai.interview.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class RecordingStorageService {

    private final Path storageDir;

    public RecordingStorageService(@Value("${recording.storage-dir}") String storageDir) {
        this.storageDir = Path.of(storageDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to create recording storage directory", e);
        }
    }

    public void save(Long sessionId, MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), pathFor(sessionId), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store recording for session " + sessionId, e);
        }
    }

    public Optional<UrlResource> load(Long sessionId) {
        Path path = pathFor(sessionId);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(new UrlResource(path.toUri()));
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path pathFor(Long sessionId) {
        return storageDir.resolve("session-" + sessionId + ".webm");
    }
}
