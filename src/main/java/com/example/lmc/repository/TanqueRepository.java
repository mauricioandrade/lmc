package com.example.lmc.repository;

import com.example.lmc.entity.Tanque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TanqueRepository extends JpaRepository<Tanque, Long> {
    List<Tanque> findByProdutoId(Long produtoId);
}
