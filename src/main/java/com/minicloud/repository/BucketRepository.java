package com.minicloud.repository;

import com.minicloud.model.AppUser;
import com.minicloud.model.Bucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BucketRepository extends JpaRepository<Bucket, Long> {
    boolean existsByNameAndOwner(String name, AppUser owner);
    List<Bucket> findByOwner(AppUser owner);
    Optional<Bucket> findByIdAndOwner(Long id, AppUser owner);
    Optional<Bucket> findByNameAndOwner(String name, AppUser owner);
}
