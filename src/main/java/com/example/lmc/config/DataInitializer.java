package com.example.lmc.config;

import com.example.lmc.entity.Bico;
import com.example.lmc.entity.Produto;
import com.example.lmc.entity.Tanque;
import com.example.lmc.repository.BicoRepository;
import com.example.lmc.repository.ProdutoRepository;
import com.example.lmc.repository.TanqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {


    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private TanqueRepository tanqueRepository;
    @Autowired
    private BicoRepository bicoRepository;

    @Override
    public void run(String... args) throws Exception {


        if (produtoRepository.count() == 0) {


            System.out.println(">>> POPULANDO BANCO DE DADOS COM DADOS DE TESTE <<<");


            Produto gasolina = new Produto();
            gasolina.setNome("Gasolina Comum");

            Produto etanol = new Produto();
            etanol.setNome("Etanol Hidratado");

            Produto dieselS500 = new Produto();
            dieselS500.setNome("Diesel S500");

            Produto dieselS10 = new Produto();
            dieselS10.setNome("Diesel S-10");


            produtoRepository.saveAll(List.of(gasolina, etanol, dieselS500, dieselS10));


            Tanque tq1_gasolina = new Tanque();
            tq1_gasolina.setNumero("TQ-01"); // Tanque 1: Gasolina
            tq1_gasolina.setCapacidadeNominal(new BigDecimal("20000.00"));
            tq1_gasolina.setProduto(gasolina); // Associa ao produto Gasolina

            Tanque tq2_etanol = new Tanque();
            tq2_etanol.setNumero("TQ-02"); // Tanque 2: Etanol
            tq2_etanol.setCapacidadeNominal(new BigDecimal("20000.00"));
            tq2_etanol.setProduto(etanol); // Associa ao produto Etanol

            Tanque tq3_dieselS500 = new Tanque();
            tq3_dieselS500.setNumero("TQ-03"); // Tanque 3: Diesel S500
            tq3_dieselS500.setCapacidadeNominal(new BigDecimal("15000.00"));
            tq3_dieselS500.setProduto(dieselS500); // Associa ao produto Diesel S500

            Tanque tq4_dieselS10 = new Tanque();
            tq4_dieselS10.setNumero("TQ-04"); // Tanque 4: Diesel S-10
            tq4_dieselS10.setCapacidadeNominal(new BigDecimal("15000.00"));
            tq4_dieselS10.setProduto(dieselS10); // Associa ao produto Diesel S-10


            tanqueRepository.saveAll(List.of(tq1_gasolina, tq2_etanol, tq3_dieselS500, tq4_dieselS10));


            Bico b01 = new Bico();
            b01.setNumero("Bico 01");
            b01.setTanque(tq1_gasolina);
            Bico b02 = new Bico();
            b02.setNumero("Bico 02");
            b02.setTanque(tq1_gasolina);
            Bico b03 = new Bico();
            b03.setNumero("Bico 03");
            b03.setTanque(tq1_gasolina);
            Bico b04 = new Bico();
            b04.setNumero("Bico 04");
            b04.setTanque(tq1_gasolina);


            Bico b05 = new Bico();
            b05.setNumero("Bico 05");
            b05.setTanque(tq2_etanol);
            Bico b06 = new Bico();
            b06.setNumero("Bico 06");
            b06.setTanque(tq2_etanol);
            Bico b07 = new Bico();
            b07.setNumero("Bico 07");
            b07.setTanque(tq2_etanol);
            Bico b08 = new Bico();
            b08.setNumero("Bico 08");
            b08.setTanque(tq2_etanol);


            Bico b09 = new Bico();
            b09.setNumero("Bico 09");
            b09.setTanque(tq4_dieselS10);
            Bico b10 = new Bico();
            b10.setNumero("Bico 10");
            b10.setTanque(tq4_dieselS10);


            Bico b11 = new Bico();
            b11.setNumero("Bico 11");
            b11.setTanque(tq3_dieselS500);
            Bico b12 = new Bico();
            b12.setNumero("Bico 12");
            b12.setTanque(tq3_dieselS500);

            bicoRepository.saveAll(List.of(
                    b01, b02, b03, b04, b05, b06, b07, b08, b09, b10, b11, b12
            ));

            System.out.println(">>> DADOS DE TESTE POPULADOS (4 Produtos, 4 Tanques, 12 Bicos) <<<");
        } else {
            System.out.println(">>> BANCO DE DADOS JÁ POPULADO. IGNORANDO DATALOADER. <<<");
        }
    }
}

