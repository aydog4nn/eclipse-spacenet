package org.dev.eclipse.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileUploadUtil {

    public static List<String> saveFiles(MultipartFile[] files, String folderName) throws IOException {
        List<String> filePaths = new ArrayList<>();

        if (files == null || files.length == 0) {
            return filePaths;
        }

        String uploadDir = "uploads/" + folderName + "/";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + sanitizeFileName(file.getOriginalFilename());
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                filePaths.add(uploadDir + fileName);
            }
        }

        return filePaths;
    }

    public static String saveSingleFile(MultipartFile file, String folderName) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String uploadDir = "uploads/" + folderName + "/";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = System.currentTimeMillis() + "_" + sanitizeFileName(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uploadDir + fileName;
    }

    private static String sanitizeFileName(String originalFileName) {
        if (originalFileName == null) return "unknown_file";
        return originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
    }
}
