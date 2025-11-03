package com.example.lmc.repository;

import com.example.lmc.entity.LmcCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface LmcCompraRepository extends JpaRepository <LmcCompra, Long> {

    Set<LmcCompra> findByLmcFolhaId(Long folhaId);

}
