package com.freetv.api;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CanalRepository extends JpaRepository<Canal, Long> {
    List<Canal> findByNombreContainingIgnoreCase(String nombre);
}