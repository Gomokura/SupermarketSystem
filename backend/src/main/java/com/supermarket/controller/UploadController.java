package com.supermarket.controller;

import com.supermarket.common.Result;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/upload")
public class UploadController {

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return Result.error("仅支持图片文件上传");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String safeExtension = StringUtils.hasText(extension) ? "." + extension.toLowerCase(Locale.ROOT) : ".jpg";

        Path uploadDir = Paths.get("uploads", "images").toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        String filename = UUID.randomUUID().toString().replace("-", "") + safeExtension;
        Path targetPath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        Map<String, String> data = new HashMap<>();
        data.put("url", "/uploads/images/" + filename);
        data.put("name", filename);
        return Result.success(data);
    }
}
