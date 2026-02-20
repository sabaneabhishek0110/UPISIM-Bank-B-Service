package com.example.icici.controller;

import com.example.icici.Repository.NpciIciciRegistryRepository;
import com.example.icici.dto.NpciPublicKeyRegistryRequest;
import com.example.icici.model.NpciIciciRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/internal/icici/npci")
public class NpciRegistryController {
    private final NpciIciciRegistryRepository repository;

    public NpciRegistryController(NpciIciciRegistryRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/register-public-key")
    public ResponseEntity<String> registerPublicKey(
            @RequestBody NpciPublicKeyRegistryRequest request
    ) {
        Optional<NpciIciciRegistry> existing =
                repository.findByNpciId(request.getNpciId());

        if (existing.isPresent()) {
            NpciIciciRegistry npci = existing.get();
            npci.setPublic_key(request.getPublicKey());
            repository.save(npci);
            return ResponseEntity.ok("Public key updated");
        }

        NpciIciciRegistry npci = new NpciIciciRegistry();
        npci.setNpciId(request.getNpciId());
        npci.setPublic_key(request.getPublicKey());
        npci.setStatus("ACTIVE");
        repository.save(npci);

        return ResponseEntity.ok("Npci public key registered at icici bank");
    }
}
