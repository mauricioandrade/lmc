package com.example.lmc.repository;

import com.example.lmc.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends JpaRepository <Produto, Long> {

    Produto findByNome(String nome);
}

