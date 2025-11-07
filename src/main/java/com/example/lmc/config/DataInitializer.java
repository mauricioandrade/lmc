package com.example.lmc.config;

import com.example.lmc.entity.Bico;
import com.example.lmc.entity.Produto;
import com.example.lmc.entity.Tanque;
import com.example.lmc.repository.BicoRepository;
import com.example.lmc.repository.ProdutoRepository;
import com.example.lmc.repository.TanqueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    private final ProdutoRepository produtoRepository;
    private final TanqueRepository tanqueRepository;
    private final BicoRepository bicoRepository;

    public DataInitializer(ProdutoRepository produtoRepository, TanqueRepository tanqueRepository, BicoRepository bicoRepository) {
        this.produtoRepository = produtoRepository;
        this.tanqueRepository = tanqueRepository;
        this.bicoRepository = bicoRepository;
    }

    @Override
    public void run(String... args) {
        if (produtoRepository.count() > 0) {
            LOGGER.info("Banco de dados já populado. Ignorando data initializer");
            return;
        }

        LOGGER.info("Populando banco de dados com dados de teste");

        Produto gasolina = novoProduto("Gasolina Comum");
        Produto etanol = novoProduto("Etanol Hidratado");
        Produto dieselS500 = novoProduto("Diesel S500");
        Produto dieselS10 = novoProduto("Diesel S-10");

        produtoRepository.saveAll(List.of(gasolina, etanol, dieselS500, dieselS10));

        Tanque tanqueGasolina = novoTanque("TQ-01", "20000.00", gasolina);
        Tanque tanqueEtanol = novoTanque("TQ-02", "20000.00", etanol);
        Tanque tanqueDieselS500 = novoTanque("TQ-03", "15000.00", dieselS500);
        Tanque tanqueDieselS10 = novoTanque("TQ-04", "15000.00", dieselS10);

        tanqueRepository.saveAll(List.of(tanqueGasolina, tanqueEtanol, tanqueDieselS500, tanqueDieselS10));

        List<Bico> bicos = new ArrayList<>();
        bicos.addAll(criarBicos(tanqueGasolina, "Bico 01", "Bico 02", "Bico 03", "Bico 04"));
        bicos.addAll(criarBicos(tanqueEtanol, "Bico 05", "Bico 06", "Bico 07", "Bico 08"));
        bicos.addAll(criarBicos(tanqueDieselS10, "Bico 09", "Bico 10"));
        bicos.addAll(criarBicos(tanqueDieselS500, "Bico 11", "Bico 12"));

        bicoRepository.saveAll(bicos);

        LOGGER.info("Dados de teste populados (4 Produtos, 4 Tanques, 12 Bicos)");
    }

    private Produto novoProduto(String nome) {
        Produto produto = new Produto();
        produto.setNome(nome);
        return produto;
    }

    private Tanque novoTanque(String numero, String capacidadeNominal, Produto produto) {
        Tanque tanque = new Tanque();
        tanque.setNumero(numero);
        tanque.setCapacidadeNominal(new BigDecimal(capacidadeNominal));
        tanque.setProduto(produto);
        return tanque;
    }

    private List<Bico> criarBicos(Tanque tanque, String... numeros) {
        return Arrays.stream(numeros)
                .map(numero -> novoBico(numero, tanque))
                .toList();
    }

    private Bico novoBico(String numero, Tanque tanque) {
        Bico bico = new Bico();
        bico.setNumero(numero);
        bico.setTanque(tanque);
        return bico;
    }
}
