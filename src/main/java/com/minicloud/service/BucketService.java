package com.minicloud.service;
import com.minicloud.model.AppUser;
import com.minicloud.model.Bucket;
import com.minicloud.repository.BucketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BucketService {
    private final BucketRepository bucketRepository;
    public BucketService(BucketRepository bucketRepository) { this.bucketRepository = bucketRepository; }

    public Bucket createBucket(AppUser owner, String name) {
        if (bucketRepository.existsByNameAndOwner(name, owner)) {
            throw new RuntimeException("Bucket with this name already exists!");
        }
        Bucket bucket = new Bucket();
        bucket.setName(name);
        bucket.setOwner(owner);
        return bucketRepository.save(bucket);
    }

    public List<Bucket> getAllBuckets(AppUser owner) {
        return bucketRepository.findByOwner(owner);
    }

    public Bucket getBucket(AppUser owner, Long id) {
        return bucketRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new RuntimeException("Bucket not found!"));
    }

    public void deleteBucket(AppUser owner, Long id) {
        Bucket bucket = getBucket(owner, id);
        bucketRepository.delete(bucket);
    }
}
