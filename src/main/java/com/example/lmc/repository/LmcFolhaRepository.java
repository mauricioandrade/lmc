package com.example.lmc.repository;

import com.example.lmc.entity.LmcFolha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LmcFolhaRepository extends JpaRepository<LmcFolha, Long> {

    Optional<LmcFolha> findByDataAndProdutoId(LocalDate data, Long produtoId);

    List<LmcFolha> findByDataBetween(LocalDate dataInicio, LocalDate dataFim);

    List<LmcFolha> findByProdutoIdAndDataBetween(Long produtoId, LocalDate dataInicio, LocalDate dataFim);

    Optional<LmcFolha> findTopByProdutoIdAndDataBeforeOrderByDataDesc(Long produtoId, LocalDate data);
}
