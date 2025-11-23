package com.sagafitmi.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sagafitmi.ecommerce.model.AllowedOrigin;

@Repository
public interface AllowedOriginRepository extends JpaRepository<AllowedOrigin, Long> {
    List<AllowedOrigin> findByEnabledTrue();
}
