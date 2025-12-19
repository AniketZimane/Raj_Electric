package com.RajBattery.RajBattery.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUploader {

    private final String DIR;

    public FileUploader(@Value("${file.upload-dir:uploads/ProductImage}") String uploadDir) {
        this.DIR = Paths.get(uploadDir).toAbsolutePath().toString();
        createDirectoryIfNotExists(DIR);
    }

    public boolean uploadFile(MultipartFile file, String fileNameNew) {
        try {
            Path destination = Paths.get(DIR, fileNameNew);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean isCreated = directory.mkdirs();
            if (!isCreated) {
                System.err.println("❌ Failed to create directory: " + directoryPath);
            }
        }
    }
}
