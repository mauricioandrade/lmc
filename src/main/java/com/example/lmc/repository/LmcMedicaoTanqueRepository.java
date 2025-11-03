package com.example.lmc.repository;

import com.example.lmc.entity.LmcMedicaoTanque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface LmcMedicaoTanqueRepository extends JpaRepository<LmcMedicaoTanque, Long> {

    Set<LmcMedicaoTanque> findByLmcFolhaId(Long folhaId);
}
