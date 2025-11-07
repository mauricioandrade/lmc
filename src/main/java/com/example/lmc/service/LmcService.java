package com.example.lmc.service;

import com.example.lmc.dto.LmcFolhaRequestDTO;
import com.example.lmc.entity.Bico;
import com.example.lmc.entity.LmcCompra;
import com.example.lmc.entity.LmcFolha;
import com.example.lmc.entity.LmcMedicaoTanque;
import com.example.lmc.entity.LmcVendaBico;
import com.example.lmc.entity.Produto;
import com.example.lmc.entity.Tanque;
import com.example.lmc.exception.BusinessException;
import com.example.lmc.repository.BicoRepository;
import com.example.lmc.repository.LmcCompraRepository;
import com.example.lmc.repository.LmcFolhaRepository;
import com.example.lmc.repository.LmcMedicaoTanqueRepository;
import com.example.lmc.repository.LmcVendaBicoRepository;
import com.example.lmc.repository.ProdutoRepository;
import com.example.lmc.repository.TanqueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class LmcService {
    private final LmcFolhaRepository lmcFolhaRepository;
    private final ProdutoRepository produtoRepository;
    private final TanqueRepository tanqueRepository;
    private final BicoRepository bicoRepository;
    private final LmcMedicaoTanqueRepository medicaoTanqueRepository;
    private final LmcCompraRepository compraRepository;
    private final LmcVendaBicoRepository vendaBicoRepository;

    private static final BigDecimal VARIACAO_PERMITIDA_PERCENTUAL = new BigDecimal("0.6");
    private static final BigDecimal CEM = new BigDecimal("100");

    @Autowired
    public LmcService(LmcFolhaRepository lmcFolhaRepository, ProdutoRepository produtoRepository,
                      TanqueRepository tanqueRepository, BicoRepository bicoRepository,
                      LmcMedicaoTanqueRepository medicaoTanqueRepository, LmcCompraRepository compraRepository,
                      LmcVendaBicoRepository vendaBicoRepository) {
        this.lmcFolhaRepository = lmcFolhaRepository;
        this.produtoRepository = produtoRepository;
        this.tanqueRepository = tanqueRepository;
        this.bicoRepository = bicoRepository;
        this.medicaoTanqueRepository = medicaoTanqueRepository;
        this.compraRepository = compraRepository;
        this.vendaBicoRepository = vendaBicoRepository;
    }

    @Transactional
    public LmcFolha salvarFolhaDiaria(LmcFolhaRequestDTO request) {
        validarFolhaUnica(request.getData(), request.getProdutoId());
        Produto produto = buscarProduto(request.getProdutoId());
        LmcFolha folha = criarFolha(request, produto);
        request.getMedicoes().stream()
                .map(medicao -> criarMedicao(folha, medicao))
                .forEach(folha.getMedicoesTanque()::add);
        Optional.ofNullable(request.getCompras()).orElseGet(List::of).stream()
                .map(compra -> criarCompra(folha, compra))
                .forEach(folha.getCompras()::add);
        request.getVendas().stream()
                .map(venda -> criarVenda(folha, venda))
                .forEach(folha.getVendasBico()::add);
        recalcularEValidarTotais(folha);
        folha.setValorAcumuladoMes(calcularValorAcumuladoMes(folha));
        return lmcFolhaRepository.save(folha);
    }

    @Transactional(readOnly = true)
    public LmcFolha buscarFolhaPorDataEProduto(LocalDate data, Long produtoId) {
        return lmcFolhaRepository.findByDataAndProdutoIdEager(data, produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Nenhuma folha LMC encontrada para esta data e produto."));
    }

    @Transactional
    public LmcMedicaoTanque atualizarMedicaoTanque(Long id, LmcFolhaRequestDTO.MedicaoTanqueDTO medicaoDTO) {
        LmcMedicaoTanque medicaoExistente = medicaoTanqueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medição não encontrada: " + id));
        medicaoExistente.setEstoqueAbertura(medicaoDTO.getEstoqueAbertura());
        medicaoExistente.setEstoqueFechamentoFisico(medicaoDTO.getEstoqueFechamentoFisico());
        if (medicaoDTO.getTanqueId() != null) {
            medicaoExistente.setTanque(buscarTanque(medicaoDTO.getTanqueId()));
        }
        LmcMedicaoTanque medicaoSalva = medicaoTanqueRepository.save(medicaoExistente);
        recalcularEValidarTotais(medicaoSalva.getLmcFolha());
        return medicaoSalva;
    }

    @Transactional
    public LmcCompra atualizarCompra(Long id, LmcFolhaRequestDTO.CompraDTO compraDTO) {
        LmcCompra compraExistente = compraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compra não encontrada: " + id));
        compraExistente.setNumeroDocumentoFiscal(compraDTO.getNumeroDocumentoFiscal());
        compraExistente.setVolumeRecebido(compraDTO.getVolumeRecebido());
        if (compraDTO.getTanqueDescargaId() != null) {
            compraExistente.setTanqueDescarga(buscarTanque(compraDTO.getTanqueDescargaId()));
        }
        LmcCompra compraSalva = compraRepository.save(compraExistente);
        recalcularEValidarTotais(compraSalva.getLmcFolha());
        return compraSalva;
    }

    @Transactional
    public LmcVendaBico atualizarVenda(Long id, LmcFolhaRequestDTO.VendaBicoDTO vendaDTO) {
        LmcVendaBico vendaExistente = vendaBicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venda não encontrada: " + id));
        vendaExistente.setEncerranteAbertura(vendaDTO.getEncerranteAbertura());
        vendaExistente.setEncerranteFechamento(vendaDTO.getEncerranteFechamento());
        vendaExistente.setAfericoes(vendaDTO.getAfericoes());
        vendaExistente.setPrecoNaBomba(vendaDTO.getPrecoNaBomba());
        vendaExistente.setVendasBico(calcularVolumeVendido(vendaDTO.getEncerranteFechamento(), vendaDTO.getEncerranteAbertura(), vendaDTO.getAfericoes()));
        if (vendaDTO.getBicoId() != null) {
            vendaExistente.setBico(buscarBico(vendaDTO.getBicoId()));
        }
        LmcVendaBico vendaSalva = vendaBicoRepository.save(vendaExistente);
        recalcularEValidarTotais(vendaSalva.getLmcFolha());
        return vendaSalva;
    }

    @Transactional
    public LmcMedicaoTanque adicionarMedicaoTanque(Long folhaId, LmcFolhaRequestDTO.MedicaoTanqueDTO medicaoDTO) {
        LmcFolha folha = lmcFolhaRepository.findById(folhaId)
                .orElseThrow(() -> new EntityNotFoundException("Folha LMC não encontrada: " + folhaId));
        LmcMedicaoTanque novaMedicao = criarMedicao(folha, medicaoDTO);
        LmcMedicaoTanque medicaoSalva = medicaoTanqueRepository.save(novaMedicao);
        folha.getMedicoesTanque().add(medicaoSalva);
        recalcularEValidarTotais(folha);
        return medicaoSalva;
    }

    @Transactional
    public LmcCompra adicionarCompra(Long folhaId, LmcFolhaRequestDTO.CompraDTO compraDTO) {
        LmcFolha folha = lmcFolhaRepository.findById(folhaId)
                .orElseThrow(() -> new EntityNotFoundException("Folha LMC não encontrada: " + folhaId));
        LmcCompra novaCompra = criarCompra(folha, compraDTO);
        LmcCompra compraSalva = compraRepository.save(novaCompra);
        folha.getCompras().add(compraSalva);
        recalcularEValidarTotais(folha);
        return compraSalva;
    }

    @Transactional
    public LmcVendaBico adicionarVenda(Long folhaId, LmcFolhaRequestDTO.VendaBicoDTO vendaDTO) {
        LmcFolha folha = lmcFolhaRepository.findById(folhaId)
                .orElseThrow(() -> new EntityNotFoundException("Folha LMC não encontrada: " + folhaId));
        LmcVendaBico novaVenda = criarVenda(folha, vendaDTO);
        LmcVendaBico vendaSalva = vendaBicoRepository.save(novaVenda);
        folha.getVendasBico().add(vendaSalva);
        recalcularEValidarTotais(folha);
        return vendaSalva;
    }

    @Transactional
    public LmcFolha atualizarObservacoes(Long folhaId, String observacoes) {
        LmcFolha folha = lmcFolhaRepository.findById(folhaId)
                .orElseThrow(() -> new EntityNotFoundException("Folha LMC não encontrada: " + folhaId));
        folha.setObservacoes(observacoes);
        validarObservacoes(folha.getVolumeDisponivel(), folha.getPerdasGanhos(), observacoes, true);
        return lmcFolhaRepository.save(folha);
    }

    @Transactional
    public void deletarMedicaoTanque(Long id) {
        LmcMedicaoTanque medicao = medicaoTanqueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medição não encontrada: " + id));
        LmcFolha folha = medicao.getLmcFolha();

        folha.getMedicoesTanque().remove(medicao);
        medicaoTanqueRepository.delete(medicao);

        recalcularEValidarTotais(folha);
    }

    @Transactional
    public void deletarCompra(Long id) {
        LmcCompra compra = compraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compra não encontrada: " + id));
        LmcFolha folha = compra.getLmcFolha();

        folha.getCompras().remove(compra);
        compraRepository.delete(compra);

        recalcularEValidarTotais(folha);
    }

    @Transactional
    public void deletarVenda(Long id) {
        LmcVendaBico venda = vendaBicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venda não encontrada: " + id));
        LmcFolha folha = venda.getLmcFolha();

        folha.getVendasBico().remove(venda);
        vendaBicoRepository.delete(venda);

        recalcularEValidarTotais(folha);
    }

    private void recalcularEValidarTotais(LmcFolha folha) {
        Set<LmcMedicaoTanque> medicoes = Objects.requireNonNullElse(folha.getMedicoesTanque(), new HashSet<>());
        BigDecimal totalEstoqueAbertura = medicoes.stream()
                .map(LmcMedicaoTanque::getEstoqueAbertura)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalEstoqueFechamento = medicoes.stream()
                .map(LmcMedicaoTanque::getEstoqueFechamentoFisico)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        folha.setEstoqueFechamento(totalEstoqueFechamento);

        Set<LmcCompra> compras = Objects.requireNonNullElse(folha.getCompras(), new HashSet<>());
        BigDecimal totalRecebido = compras.stream()
                .map(LmcCompra::getVolumeRecebido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        folha.setTotalRecebido(totalRecebido);

        BigDecimal volumeDisponivel = totalEstoqueAbertura.add(totalRecebido);
        folha.setVolumeDisponivel(volumeDisponivel);

        Set<LmcVendaBico> vendas = Objects.requireNonNullElse(folha.getVendasBico(), new HashSet<>());
        BigDecimal totalVendasDia = BigDecimal.ZERO;
        BigDecimal valorVendasDia = BigDecimal.ZERO;
        for (LmcVendaBico venda : vendas) {
            BigDecimal vendasCalculadas = calcularVolumeVendido(venda.getEncerranteFechamento(), venda.getEncerranteAbertura(), venda.getAfericoes());
            venda.setVendasBico(vendasCalculadas);
            totalVendasDia = totalVendasDia.add(vendasCalculadas);
            valorVendasDia = valorVendasDia.add(vendasCalculadas.multiply(venda.getPrecoNaBomba()));
        }
        folha.setTotalVendasDia(totalVendasDia);
        folha.setValorVendasDia(valorVendasDia.setScale(2, RoundingMode.HALF_UP));

        BigDecimal estoqueEscritural = volumeDisponivel.subtract(totalVendasDia);
        folha.setEstoqueEscritural(estoqueEscritural);

        BigDecimal perdasGanhos = totalEstoqueFechamento.subtract(estoqueEscritural);
        folha.setPerdasGanhos(perdasGanhos);

        boolean novaFolha = folha.getId() == null;
        validarObservacoes(volumeDisponivel, perdasGanhos, folha.getObservacoes(), novaFolha);
    }

    private void validarFolhaUnica(LocalDate data, Long produtoId) {
        lmcFolhaRepository.findByDataAndProdutoId(data, produtoId)
                .ifPresent(folha -> {
                    throw new BusinessException("Já existe uma folha LMC para este produto e data.");
                });
    }

    private Produto buscarProduto(Long produtoId) {
        return produtoRepository.findById(produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
    }

    private Tanque buscarTanque(Long tanqueId) {
        return tanqueRepository.findById(tanqueId)
                .orElseThrow(() -> new EntityNotFoundException("Tanque não encontrado"));
    }

    private Bico buscarBico(Long bicoId) {
        return bicoRepository.findById(bicoId)
                .orElseThrow(() -> new EntityNotFoundException("Bico não encontrado"));
    }

    private LmcFolha criarFolha(LmcFolhaRequestDTO request, Produto produto) {
        LmcFolha folha = new LmcFolha();
        folha.setData(request.getData());
        folha.setProduto(produto);
        folha.setObservacoes(request.getObservacoes());
        folha.setMedicoesTanque(new HashSet<>());
        folha.setCompras(new HashSet<>());
        folha.setVendasBico(new HashSet<>());
        return folha;
    }

    private LmcMedicaoTanque criarMedicao(LmcFolha folha, LmcFolhaRequestDTO.MedicaoTanqueDTO medicaoDTO) {
        LmcMedicaoTanque medicao = new LmcMedicaoTanque();
        medicao.setLmcFolha(folha);
        medicao.setTanque(buscarTanque(medicaoDTO.getTanqueId()));
        medicao.setEstoqueAbertura(medicaoDTO.getEstoqueAbertura());
        medicao.setEstoqueFechamentoFisico(medicaoDTO.getEstoqueFechamentoFisico());
        return medicao;
    }

    private LmcCompra criarCompra(LmcFolha folha, LmcFolhaRequestDTO.CompraDTO compraDTO) {
        LmcCompra compra = new LmcCompra();
        compra.setLmcFolha(folha);
        compra.setTanqueDescarga(buscarTanque(compraDTO.getTanqueDescargaId()));
        compra.setNumeroDocumentoFiscal(compraDTO.getNumeroDocumentoFiscal());
        compra.setVolumeRecebido(compraDTO.getVolumeRecebido());
        return compra;
    }

    private LmcVendaBico criarVenda(LmcFolha folha, LmcFolhaRequestDTO.VendaBicoDTO vendaDTO) {
        LmcVendaBico venda = new LmcVendaBico();
        venda.setLmcFolha(folha);
        venda.setBico(buscarBico(vendaDTO.getBicoId()));
        venda.setPrecoNaBomba(vendaDTO.getPrecoNaBomba());
        venda.setEncerranteAbertura(vendaDTO.getEncerranteAbertura());
        venda.setEncerranteFechamento(vendaDTO.getEncerranteFechamento());
        venda.setAfericoes(vendaDTO.getAfericoes());
        venda.setVendasBico(calcularVolumeVendido(vendaDTO.getEncerranteFechamento(), vendaDTO.getEncerranteAbertura(), vendaDTO.getAfericoes()));
        return venda;
    }

    private BigDecimal calcularVolumeVendido(BigDecimal encerranteFechamento, BigDecimal encerranteAbertura, BigDecimal afericoes) {
        BigDecimal fechamento = new BigDecimal(String.valueOf(encerranteFechamento));
        BigDecimal abertura = new BigDecimal(String.valueOf(encerranteAbertura));
        return fechamento.subtract(abertura).subtract(afericoes);
    }

    private BigDecimal calcularValorAcumuladoMes(LmcFolha folha) {
        return lmcFolhaRepository.findTopByProdutoIdAndDataBeforeOrderByDataDesc(folha.getProduto().getId(), folha.getData())
                .filter(folhaAnterior -> folhaAnterior.getData().getMonth().equals(folha.getData().getMonth()))
                .map(LmcFolha::getValorAcumuladoMes)
                .orElse(BigDecimal.ZERO)
                .add(folha.getValorVendasDia());
    }

    private void validarObservacoes(BigDecimal volumeDisponivel, BigDecimal perdasGanhos, String observacoes, boolean observacaoObrigatoria) {
        if (volumeDisponivel == null || volumeDisponivel.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal perdas = Optional.ofNullable(perdasGanhos).orElse(BigDecimal.ZERO);
        BigDecimal variacaoPercentual = perdas.abs()
                .divide(volumeDisponivel, 6, RoundingMode.HALF_UP)
                .multiply(CEM);
        if (variacaoPercentual.compareTo(VARIACAO_PERMITIDA_PERCENTUAL) > 0 && observacaoObrigatoria) {
            if (observacoes == null || observacoes.trim().isEmpty()) {
                throw new BusinessException(String.format("Variação de estoque superior a 0.6%% (%s%%). O campo Observações (13.5) é obrigatório.",
                        variacaoPercentual.setScale(3, RoundingMode.HALF_UP)));
            }
        }
    }
}
