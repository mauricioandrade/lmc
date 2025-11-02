package com.example.lmc.repository;

import com.example.lmc.entity.LmcFolha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LmcFolhaRepository extends JpaRepository<LmcFolha, Long> {

    Optional<LmcFolha> findByDataAndProdutoId(LocalDate data, Long produtoId);

    @Query("SELECT DISTINCT f FROM LmcFolha f " + // <-- ADICIONE DISTINCT AQUI
            "JOIN FETCH f.produto " +
            "JOIN FETCH f.medicoesTanque " +
            "WHERE f.data BETWEEN :dataInicio AND :dataFim " +
            "ORDER BY f.data, f.produto.nome")
    List<LmcFolha> findByDataBetweenEager(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    List<LmcFolha> findByDataBetween(LocalDate dataInicio, LocalDate dataFim);

    List<LmcFolha> findByProdutoIdAndDataBetween(Long produtoId, LocalDate dataInicio, LocalDate dataFim);

    Optional<LmcFolha> findTopByProdutoIdAndDataBeforeOrderByDataDesc(Long produtoId, LocalDate data);
}
