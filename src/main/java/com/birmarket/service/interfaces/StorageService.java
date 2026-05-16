package com.birmarket.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(String bucketName, String objectName, MultipartFile file);
    void deleteFile(String bucketName, String objectName);
}
