package com.example.lmc.repository;

import com.example.lmc.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByCnpj(String cnpj);

    Optional<Empresa> findFirstByIsAtivaTrue();


    @Query("SELECT e FROM Empresa e ORDER BY e.razaoSocial")
    List<Empresa> findAllOrderByRazaoSocial();
}
