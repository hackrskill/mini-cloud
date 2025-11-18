package com.minicloud.repository;

import com.minicloud.model.AppUser;
import com.minicloud.model.S3Object;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface S3ObjectRepository extends JpaRepository<S3Object, Long> {
    List<S3Object> findByOwnerAndBucketName(AppUser owner, String bucketName);
    Optional<S3Object> findByObjectKeyAndOwner(String objectKey, AppUser owner);
    Optional<S3Object> findByObjectKey(String objectKey);
    List<S3Object> findByBucketName(String bucketName);
}