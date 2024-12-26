package com.RajBattery.RajBattery.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUploader {

    private final String DIR;

    public FileUploader() throws IOException {
        // Get the directory path
        this.DIR = new ClassPathResource("static/assestad/img/ProductImage/").getFile().getAbsolutePath();
        // Ensure the directory exists
        createDirectoryIfNotExists(DIR);
    }

    public boolean uploadFile(MultipartFile file, String fileNameNew) {
        boolean result = false;

        try {
            Files.copy(file.getInputStream(), Paths.get(DIR + File.separator + fileNameNew), StandardCopyOption.REPLACE_EXISTING);
            result = true;
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
        }

        return result;
    }

    private void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean isCreated = directory.mkdirs();
            if (!isCreated) {
                System.err.println("Failed to create directory: " + directoryPath);
            }
        }
    }
}
