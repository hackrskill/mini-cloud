package com.minicloud.service;
import com.minicloud.model.S3Object;
import com.minicloud.repository.S3ObjectRepository;
import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Service
public class StorageService {
    private final MinioClient minioClient;
    private final S3ObjectRepository s3ObjectRepository;
    @Value("${minio.bucket.default:mini-cloud}")
    private String defaultBucket;
    public StorageService(MinioClient minioClient, S3ObjectRepository s3ObjectRepository) {
        this.minioClient = minioClient;
        this.s3ObjectRepository = s3ObjectRepository;
    }
    private boolean minioAvailable() {
        try { minioClient.listBuckets(); return true; } catch (Exception e) { return false; }
    }
    public S3Object uploadFile(String bucketName, MultipartFile file) {
        try {
            if (bucketName == null || bucketName.isEmpty()) bucketName = defaultBucket;
            if (minioAvailable()) {
                boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
                if (!exists) minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                String objectKey = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
                minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectKey).stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build());
                S3Object meta = new S3Object(); meta.setObjectKey(objectKey); meta.setBucketName(bucketName); meta.setFileName(file.getOriginalFilename()); meta.setContentType(file.getContentType()); meta.setSize(file.getSize()); meta.setEtag(UUID.randomUUID().toString()); meta.setCreatedAt(LocalDateTime.now());
                return s3ObjectRepository.save(meta);
            } else {
                File dir = new File("storage/"+bucketName); dir.mkdirs();
                File dest = new File(dir, UUID.randomUUID().toString()+"-"+file.getOriginalFilename());
                try (FileOutputStream fos = new FileOutputStream(dest); InputStream is = file.getInputStream()) { is.transferTo(fos); }
                S3Object meta = new S3Object(); meta.setObjectKey(dest.getAbsolutePath()); meta.setBucketName(bucketName); meta.setFileName(file.getOriginalFilename()); meta.setContentType(file.getContentType()); meta.setSize(file.getSize()); meta.setEtag(UUID.randomUUID().toString()); meta.setCreatedAt(LocalDateTime.now());
                return s3ObjectRepository.save(meta);
            }
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }
    public Optional<S3Object> getMetadata(String objectKey) { return s3ObjectRepository.findByObjectKey(objectKey); }
    public List<S3Object> listFiles(String bucketName) { return s3ObjectRepository.findByBucketName(bucketName); }
}
