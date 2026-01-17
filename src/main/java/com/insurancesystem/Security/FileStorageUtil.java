package com.insurancesystem.Security;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

public class FileStorageUtil {

    public static String save(MultipartFile file, String folder) {
        try {
            String filename =
                    UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path dir = Paths.get("uploads", folder);
            Files.createDirectories(dir);

            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + folder + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
