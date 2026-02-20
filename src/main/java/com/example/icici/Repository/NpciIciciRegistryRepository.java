package com.example.icici.Repository;

import com.example.icici.model.NpciIciciRegistry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NpciIciciRegistryRepository extends JpaRepository<NpciIciciRegistry,String> {
    Optional<NpciIciciRegistry> findByNpciId(String npciId);
}
