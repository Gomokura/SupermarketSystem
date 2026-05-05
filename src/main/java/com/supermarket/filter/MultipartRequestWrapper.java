package com.supermarket.filter;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

/**
 * MultipartRequestWrapper - 将 ServletFileUpload 解析的结果包装为标准 HttpServletRequest
 * 使得通过 getParameter() 仍可获取表单字段值
 */
public class MultipartRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String[]> params = new HashMap<>();
    private final Map<String, List<FileItem>> fileItems = new HashMap<>();

    public MultipartRequestWrapper(HttpServletRequest request) {
        super(request);
        parseRequest(request);
    }

    private void parseRequest(HttpServletRequest request) {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(1024 * 1024 * 10); // 10MB 内存阈值
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(1024 * 1024 * 10); // 单文件最大 10MB

        try {
            List<FileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                if (item.isFormField()) {
                    // 普通字段
                    String name = item.getFieldName();
                    String value = item.getString("UTF-8");
                    params.computeIfAbsent(name, k -> new String[0]);
                    // 追加到现有值
                    String[] old = params.get(name);
                    String[] newVals = Arrays.copyOf(old, old.length + 1);
                    newVals[old.length] = value;
                    params.put(name, newVals);
                } else {
                    // 文件字段
                    fileItems.put(item.getFieldName(), Collections.singletonList(item));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getParameter(String name) {
        String[] vals = params.get(name);
        return vals != null && vals.length > 0 ? vals[0] : null;
    }

    @Override
    public String[] getParameterValues(String name) {
        return params.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return new HashMap<>(params);
    }

    public List<FileItem> getFileItems(String fieldName) {
        return fileItems.get(fieldName);
    }

    public FileItem getFileItem(String fieldName) {
        List<FileItem> list = fileItems.get(fieldName);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }
}
