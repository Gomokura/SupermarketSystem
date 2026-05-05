package com.supermarket.servlet;

import com.supermarket.servlet.BaseServlet;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * UploadServlet - 文件上传
 * action=upload
 */
public class UploadServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uploadPath = req.getServletContext().getRealPath("/uploads");
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(1024 * 1024 * 10);
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(1024 * 1024 * 10);

            java.util.List<FileItem> items = upload.parseRequest(req);
            for (FileItem item : items) {
                if (!item.isFormField()) {
                    String fileName = new File(item.getName()).getName();
                    String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";
                    String savedName = UUID.randomUUID().toString().replace("-", "") + ext;
                    File savedFile = new File(uploadDir, savedName);
                    item.write(savedFile);

                    Map<String, Object> data = new HashMap<>();
                    data.put("url", req.getContextPath() + "/uploads/" + savedName);
                    data.put("filename", savedName);
                    json(req, resp, data);
                    return;
                }
            }
            jsonError(resp, "未找到上传文件");
        } catch (Exception e) {
            jsonError(resp, "上传失败: " + e.getMessage());
        }
    }
}
