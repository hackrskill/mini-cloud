package com.minicloud.controller;
import com.minicloud.model.S3Object;
import com.minicloud.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/storage")  // Changed from "/api/s3" to avoid conflict
public class StorageController {
    private final StorageService storageService;
    public StorageController(StorageService storageService) { this.storageService = storageService; }
    @PostMapping("/buckets/{bucketName}/upload")
    public ResponseEntity<S3Object> upload(@PathVariable String bucketName, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(storageService.uploadFile(bucketName, file));
    }
    @GetMapping("/buckets/{bucketName}/objects")
    public List<S3Object> list(@PathVariable String bucketName) { return storageService.listFiles(bucketName); }
}
