package com.example.lmc.repository;

import com.example.lmc.entity.LmcVendaBico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface LmcVendaBicoRepository extends JpaRepository<LmcVendaBico, Long> {

    Set<LmcVendaBico> findByLmcFolhaId(Long folhaId);

    boolean existsByBicoId(Long bicoId);
}
