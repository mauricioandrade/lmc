package com.example.lmc.config;

import com.example.lmc.entity.Bico;
import com.example.lmc.entity.Produto;
import com.example.lmc.entity.Tanque;
import com.example.lmc.repository.BicoRepository;
import com.example.lmc.repository.ProdutoRepository;
import com.example.lmc.repository.TanqueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private TanqueRepository tanqueRepository;
    @Autowired
    private BicoRepository bicoRepository;

    @Override
    public void run(String... args) {
        if (produtoRepository.count() == 0) {
            LOGGER.info("Populando banco de dados com dados de teste");

            Produto gasolina = new Produto();
            gasolina.setNome("Gasolina Comum");

            Produto etanol = new Produto();
            etanol.setNome("Etanol Hidratado");

            Produto dieselS500 = new Produto();
            dieselS500.setNome("Diesel S500");

            Produto dieselS10 = new Produto();
            dieselS10.setNome("Diesel S-10");

            produtoRepository.saveAll(List.of(gasolina, etanol, dieselS500, dieselS10));

            Tanque tq1Gasolina = new Tanque();
            tq1Gasolina.setNumero("TQ-01");
            tq1Gasolina.setCapacidadeNominal(new BigDecimal("20000.00"));
            tq1Gasolina.setProduto(gasolina);

            Tanque tq2Etanol = new Tanque();
            tq2Etanol.setNumero("TQ-02");
            tq2Etanol.setCapacidadeNominal(new BigDecimal("20000.00"));
            tq2Etanol.setProduto(etanol);

            Tanque tq3DieselS500 = new Tanque();
            tq3DieselS500.setNumero("TQ-03");
            tq3DieselS500.setCapacidadeNominal(new BigDecimal("15000.00"));
            tq3DieselS500.setProduto(dieselS500);

            Tanque tq4DieselS10 = new Tanque();
            tq4DieselS10.setNumero("TQ-04");
            tq4DieselS10.setCapacidadeNominal(new BigDecimal("15000.00"));
            tq4DieselS10.setProduto(dieselS10);

            tanqueRepository.saveAll(List.of(tq1Gasolina, tq2Etanol, tq3DieselS500, tq4DieselS10));

            Bico b01 = new Bico();
            b01.setNumero("Bico 01");
            b01.setTanque(tq1Gasolina);
            Bico b02 = new Bico();
            b02.setNumero("Bico 02");
            b02.setTanque(tq1Gasolina);
            Bico b03 = new Bico();
            b03.setNumero("Bico 03");
            b03.setTanque(tq1Gasolina);
            Bico b04 = new Bico();
            b04.setNumero("Bico 04");
            b04.setTanque(tq1Gasolina);

            Bico b05 = new Bico();
            b05.setNumero("Bico 05");
            b05.setTanque(tq2Etanol);
            Bico b06 = new Bico();
            b06.setNumero("Bico 06");
            b06.setTanque(tq2Etanol);
            Bico b07 = new Bico();
            b07.setNumero("Bico 07");
            b07.setTanque(tq2Etanol);
            Bico b08 = new Bico();
            b08.setNumero("Bico 08");
            b08.setTanque(tq2Etanol);

            Bico b09 = new Bico();
            b09.setNumero("Bico 09");
            b09.setTanque(tq4DieselS10);
            Bico b10 = new Bico();
            b10.setNumero("Bico 10");
            b10.setTanque(tq4DieselS10);

            Bico b11 = new Bico();
            b11.setNumero("Bico 11");
            b11.setTanque(tq3DieselS500);
            Bico b12 = new Bico();
            b12.setNumero("Bico 12");
            b12.setTanque(tq3DieselS500);

            bicoRepository.saveAll(List.of(
                    b01, b02, b03, b04, b05, b06, b07, b08, b09, b10, b11, b12
            ));

            LOGGER.info("Dados de teste populados (4 Produtos, 4 Tanques, 12 Bicos)");
        } else {
            LOGGER.info("Banco de dados já populado. Ignorando data initializer");
        }
    }
}
