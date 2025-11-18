package com.minicloud.repository;

import com.minicloud.model.AppUser;
import com.minicloud.model.LambdaFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LambdaFunctionRepository extends JpaRepository<LambdaFunction, Long> {
    Optional<LambdaFunction> findByFunctionNameAndOwner(String functionName, AppUser owner);
    boolean existsByFunctionNameAndOwner(String functionName, AppUser owner);
    List<LambdaFunction> findByOwner(AppUser owner);
}
