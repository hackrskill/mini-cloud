package com.minicloud.controller;
import com.minicloud.model.AppUser;
import com.minicloud.model.Bucket;
import com.minicloud.service.AuthService;
import com.minicloud.service.BucketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/buckets")
public class BucketController {
    private final BucketService bucketService;
    private final AuthService authService;
    public BucketController(BucketService bucketService, AuthService authService) {
        this.bucketService = bucketService;
        this.authService = authService;
    }
    @PostMapping
    public ResponseEntity<Bucket> createBucket(@RequestHeader("X-Auth-Token") String token,
                                               @RequestParam String name) {
        AppUser user = authService.requireUser(token);
        return ResponseEntity.ok(bucketService.createBucket(user, name));
    }
    @GetMapping
    public List<Bucket> listBuckets(@RequestHeader("X-Auth-Token") String token) {
        AppUser user = authService.requireUser(token);
        return bucketService.getAllBuckets(user);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Bucket> getBucket(@RequestHeader("X-Auth-Token") String token,
                                            @PathVariable Long id) {
        AppUser user = authService.requireUser(token);
        return ResponseEntity.ok(bucketService.getBucket(user, id));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBucket(@RequestHeader("X-Auth-Token") String token,
                                             @PathVariable Long id) {
        AppUser user = authService.requireUser(token);
        bucketService.deleteBucket(user, id);
        return ResponseEntity.ok().build();
    }
}
