package com.supermarket.controller;

import com.supermarket.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UploadController {

    private static final String UPLOAD_DIR = "./uploads/";

    @PostMapping("/upload")
    public Result<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String newFilename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(UPLOAD_DIR, newFilename);

            Files.copy(file.getInputStream(), filePath);

            String fileUrl = "/uploads/" + newFilename;
            return Result.success(fileUrl);

        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}