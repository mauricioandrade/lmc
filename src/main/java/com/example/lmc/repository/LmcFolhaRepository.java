package com.example.lmc.repository;

import com.example.lmc.entity.LmcFolha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface LmcFolhaRepository extends JpaRepository<LmcFolha, Long> {

    Optional<LmcFolha> findByDataAndProdutoId(LocalDate data, Long produtoId);

    @Query("SELECT DISTINCT f FROM LmcFolha f " +
            "LEFT JOIN FETCH f.produto " +
            "LEFT JOIN FETCH f.medicoesTanque mt " +
            "LEFT JOIN FETCH mt.tanque " +
            "LEFT JOIN FETCH f.vendasBico vb " +
            "LEFT JOIN FETCH vb.bico b " +
            "LEFT JOIN FETCH b.tanque " +
            "LEFT JOIN FETCH f.compras c " +
            "LEFT JOIN FETCH c.tanqueDescarga " +
            "WHERE f.data BETWEEN :dataInicio AND :dataFim " +
            "ORDER BY f.data, f.produto.nome")
    Set<LmcFolha> findByDataBetweenEager(
                                          @Param("dataInicio") LocalDate dataInicio,
                                          @Param("dataFim") LocalDate dataFim
    );

    @Query("SELECT f FROM LmcFolha f " +
            "LEFT JOIN FETCH f.produto p " + // Adicionado fetch do produto
            "LEFT JOIN FETCH f.medicoesTanque mt " +
            "LEFT JOIN FETCH mt.tanque " +
            "LEFT JOIN FETCH f.vendasBico vb " +
            "LEFT JOIN FETCH vb.bico b " +
            "LEFT JOIN FETCH b.tanque " +
            "LEFT JOIN FETCH f.compras c " +
            "LEFT JOIN FETCH c.tanqueDescarga " +
            "WHERE f.data = :data AND f.produto.id = :produtoId")
    Optional<LmcFolha> findByDataAndProdutoIdEager(
            @Param("data") LocalDate data,
            @Param("produtoId") Long produtoId
    );

    List<LmcFolha> findByDataBetween(LocalDate dataInicio, LocalDate dataFim);

    List<LmcFolha> findByProdutoIdAndDataBetween(Long produtoId, LocalDate dataInicio, LocalDate dataFim);

    Optional<LmcFolha> findTopByProdutoIdAndDataBeforeOrderByDataDesc(Long produtoId, LocalDate data);
}
