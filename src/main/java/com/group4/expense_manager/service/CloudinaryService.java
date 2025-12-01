package com.group4.expense_manager.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadIcon(MultipartFile file, String folder) {
        try {
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image"
            );
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Upload icon thất bại", e);
        }
    }
    public void deleteIcon(String iconUrl) {
        if (iconUrl == null || iconUrl.isBlank()) return;
        String publicId = extractPublicId(iconUrl);
        if (publicId == null) return;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            // log if needed
        }
    }

    private String extractPublicId(String url) {
        try {
            int uploadIdx = url.indexOf("/upload/");
            if (uploadIdx == -1) return null;
            String afterUpload = url.substring(uploadIdx + 8);
            // remove version segment (starts with v + digits + /)
            if (afterUpload.startsWith("v")) {
                int slash = afterUpload.indexOf('/');
                if (slash != -1) afterUpload = afterUpload.substring(slash + 1);
            }
            // strip extension
            int dot = afterUpload.lastIndexOf('.');
            if (dot != -1) afterUpload = afterUpload.substring(0, dot);
            return afterUpload;
        } catch (Exception e) {
            return null;
        }
    }
}