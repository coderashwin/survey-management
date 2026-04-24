package com.sbi.branchdarpan.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sbi.branchdarpan.exception.ResourceNotFoundException;

@Service
public class FileStorageService {

    private final Path uploadRoot = Path.of("uploads");

    public FileStorageService() throws IOException {
        Files.createDirectories(uploadRoot);
    }

    public String store(MultipartFile file) {
        try {
            String safeName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path target = uploadRoot.resolve(safeName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + safeName;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store file", exception);
        }
    }

    public Resource load(String filename) {
        try {
            Path path = uploadRoot.resolve(filename);
            if (!Files.exists(path)) {
                throw new ResourceNotFoundException("File not found");
            }
            return new UrlResource(path.toUri());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load file", exception);
        }
    }
}
