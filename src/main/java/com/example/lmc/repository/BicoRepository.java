package com.example.lmc.repository;

import com.example.lmc.entity.Bico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BicoRepository extends JpaRepository<Bico, Long> {
    List<Bico> findByTanqueId(Long tanqueId);
}
