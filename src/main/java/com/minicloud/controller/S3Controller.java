package com.minicloud.controller;

import com.minicloud.model.AppUser;
import com.minicloud.model.S3Object;
import com.minicloud.service.AuthService;
import com.minicloud.service.S3Service;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Service s3Service;
    private final AuthService authService;

    public S3Controller(S3Service s3Service, AuthService authService) {
        this.s3Service = s3Service;
        this.authService = authService;
    }

    @PostMapping("/buckets/{bucket}/upload")
    public ResponseEntity<?> upload(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable String bucket,
            @RequestParam("file") MultipartFile file
    ) {
        AppUser user = authService.requireUser(token);
        S3Object result = s3Service.uploadFile(user, bucket, file, false);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/buckets/{bucket}/objects")
    public List<S3Object> listObjects(@RequestHeader("X-Auth-Token") String token,
                                      @PathVariable String bucket) {
        AppUser user = authService.requireUser(token);
        return s3Service.listFiles(user, bucket);
    }

    @GetMapping("/objects/{objectKey}/download")
    public ResponseEntity<InputStreamResource> download(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestParam(value = "token", required = false) String tokenQuery,
            @PathVariable String objectKey) {
        AppUser user = authService.requireUser(token != null ? token : tokenQuery);
        InputStream stream = s3Service.downloadFile(user, objectKey);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + objectKey)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }

    @DeleteMapping("/objects/{objectKey}")
    public ResponseEntity<Void> deleteObject(@RequestHeader("X-Auth-Token") String token,
                                             @PathVariable String objectKey) {
        AppUser user = authService.requireUser(token);
        s3Service.deleteFile(user, objectKey);
        return ResponseEntity.ok().build();
    }
}