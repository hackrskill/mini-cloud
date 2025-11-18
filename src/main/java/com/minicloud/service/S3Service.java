package com.minicloud.service;

import com.minicloud.model.AppUser;
import com.minicloud.model.Bucket;
import com.minicloud.model.S3Object;
import com.minicloud.repository.BucketRepository;
import com.minicloud.repository.S3ObjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class S3Service {

    private final S3ObjectRepository repo;
    private final BucketRepository bucketRepository;
    private final String STORAGE_PATH;

    public S3Service(S3ObjectRepository repo, BucketRepository bucketRepository) {
        this.repo = repo;
        this.bucketRepository = bucketRepository;

        String userDir = System.getProperty("user.dir");
        File projectRoot = new File(userDir);
        if (userDir.contains("target")) {
            projectRoot = projectRoot.getParentFile();
        }
        File storageDir = new File(projectRoot, "storage");
        this.STORAGE_PATH = storageDir.getAbsolutePath() + File.separator;
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    public S3Object uploadFile(AppUser owner, String bucket, MultipartFile file, boolean isPublic) {
        try {
            Bucket bucketEntity = bucketRepository.findByNameAndOwner(bucket, owner)
                    .orElseThrow(() -> new RuntimeException("Bucket '" + bucket + "' does not exist. Please create it first."));

            if (file == null || file.isEmpty()) {
                throw new RuntimeException("File is empty or null");
            }

            File folder = new File(STORAGE_PATH + bucketEntity.getName());
            if (!folder.exists() && !folder.mkdirs()) {
                throw new RuntimeException("Failed to create storage directory: " + folder.getAbsolutePath());
            }

            String key = UUID.randomUUID() + "-" + (file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");

            File saved = new File(folder, key);
            file.transferTo(saved.toPath());

            S3Object obj = new S3Object();
            obj.setOwner(owner);
            obj.setBucketName(bucketEntity.getName());
            obj.setFileName(file.getOriginalFilename());
            obj.setContentType(file.getContentType());
            obj.setSize(file.getSize());
            obj.setObjectKey(key);

            return repo.save(obj);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage() + " (Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "unknown") + ")", e);
        }
    }

    public List<S3Object> listFiles(AppUser owner, String bucket) {
        return repo.findByOwnerAndBucketName(owner, bucket);
    }

    public InputStream downloadFile(AppUser owner, String objectKey) {
        try {
            S3Object obj = repo.findByObjectKeyAndOwner(objectKey, owner)
                    .orElseThrow(() -> new RuntimeException("File not found: " + objectKey));
            File file = new File(STORAGE_PATH + obj.getBucketName() + File.separator + obj.getObjectKey());
            if (!file.exists()) {
                throw new RuntimeException("Physical file not found: " + file.getAbsolutePath());
            }
            return new FileInputStream(file);
        } catch (Exception e) {
            throw new RuntimeException("File not found: " + e.getMessage(), e);
        }
    }

    public void deleteFile(AppUser owner, String objectKey) {
        S3Object obj = repo.findByObjectKeyAndOwner(objectKey, owner)
                .orElseThrow(() -> new RuntimeException("File not found: " + objectKey));
        File file = new File(STORAGE_PATH + obj.getBucketName() + File.separator + obj.getObjectKey());
        if (file.exists()) {
            file.delete();
        }
        repo.delete(obj);
    }
}