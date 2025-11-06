package com.example.lmc.repository;

import com.example.lmc.entity.Tanque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TanqueRepository extends JpaRepository<Tanque, Long> {

    @Query("SELECT t FROM Tanque t LEFT JOIN FETCH t.produto WHERE t.produto.id = :produtoId")
    List<Tanque> findByProdutoIdEager(@Param("produtoId") Long produtoId);

    Optional<Tanque> findByNumero(String numero);

    @Query("SELECT t FROM Tanque t LEFT JOIN FETCH t.produto")
    List<Tanque> findAllWithProduto();
}
