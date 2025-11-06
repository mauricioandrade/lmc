package com.example.lmc.repository;

import com.example.lmc.entity.Bico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BicoRepository extends JpaRepository<Bico, Long> {

    List<Bico> findByTanqueId(Long tanqueId);
    boolean existsByTanqueId(Long tanqueId);
    Optional<Bico> findByNumero(String numero);

    @Query("SELECT b FROM Bico b LEFT JOIN FETCH b.tanque t LEFT JOIN FETCH t.produto")
    List<Bico> findAllWithTanqueAndProduto();
}
