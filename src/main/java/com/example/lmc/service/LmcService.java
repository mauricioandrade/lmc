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
import com.example.lmc.service.support.LmcFolhaCalculator;
import com.example.lmc.service.support.LmcFolhaFactory;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
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
    private final LmcFolhaFactory folhaFactory;
    private final LmcFolhaCalculator folhaCalculator;

    private static final BigDecimal VARIACAO_PERMITIDA_PERCENTUAL = new BigDecimal("0.6");
    private static final BigDecimal CEM = new BigDecimal("100");

    public LmcService(LmcFolhaRepository lmcFolhaRepository, ProdutoRepository produtoRepository,
                      TanqueRepository tanqueRepository, BicoRepository bicoRepository,
                      LmcMedicaoTanqueRepository medicaoTanqueRepository, LmcCompraRepository compraRepository,
                      LmcVendaBicoRepository vendaBicoRepository, LmcFolhaFactory folhaFactory,
                      LmcFolhaCalculator folhaCalculator) {
        this.lmcFolhaRepository = lmcFolhaRepository;
        this.produtoRepository = produtoRepository;
        this.tanqueRepository = tanqueRepository;
        this.bicoRepository = bicoRepository;
        this.medicaoTanqueRepository = medicaoTanqueRepository;
        this.compraRepository = compraRepository;
        this.vendaBicoRepository = vendaBicoRepository;
        this.folhaFactory = folhaFactory;
        this.folhaCalculator = folhaCalculator;
    }

    @Transactional
    public LmcFolha salvarFolhaDiaria(LmcFolhaRequestDTO request) {
        validarFolhaUnica(request.getData(), request.getProdutoId());
        Produto produto = buscarProduto(request.getProdutoId());
        LmcFolha folha = folhaFactory.criarFolha(request, produto);
        request.getMedicoes().forEach(medicao -> {
            LmcMedicaoTanque medicaoCriada = folhaFactory.criarMedicao(
                    folha,
                    medicao,
                    buscarTanque(medicao.getTanqueId())
            );
            folha.getMedicoesTanque().add(medicaoCriada);
        });
        Optional.ofNullable(request.getCompras()).orElseGet(List::of).forEach(compra -> {
            LmcCompra compraCriada = folhaFactory.criarCompra(
                    folha,
                    compra,
                    buscarTanque(compra.getTanqueDescargaId())
            );
            folha.getCompras().add(compraCriada);
        });
        request.getVendas().forEach(venda -> {
            LmcVendaBico vendaCriada = folhaFactory.criarVenda(
                    folha,
                    venda,
                    buscarBico(venda.getBicoId()),
                    folhaCalculator.calcularVolumeVendido(
                            venda.getEncerranteFechamento(),
                            venda.getEncerranteAbertura(),
                            venda.getAfericoes()
                    )
            );
            folha.getVendasBico().add(vendaCriada);
        });
        atualizarFolha(folha);
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
        atualizarFolha(medicaoSalva.getLmcFolha());
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
        atualizarFolha(compraSalva.getLmcFolha());
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
        vendaExistente.setVendasBico(folhaCalculator.calcularVolumeVendido(
                vendaDTO.getEncerranteFechamento(),
                vendaDTO.getEncerranteAbertura(),
                vendaDTO.getAfericoes()
        ));
        if (vendaDTO.getBicoId() != null) {
            vendaExistente.setBico(buscarBico(vendaDTO.getBicoId()));
        }
        LmcVendaBico vendaSalva = vendaBicoRepository.save(vendaExistente);
        atualizarFolha(vendaSalva.getLmcFolha());
        return vendaSalva;
    }

    @Transactional
    public LmcMedicaoTanque adicionarMedicaoTanque(Long folhaId, LmcFolhaRequestDTO.MedicaoTanqueDTO medicaoDTO) {
        LmcFolha folha = lmcFolhaRepository.findById(folhaId)
                .orElseThrow(() -> new EntityNotFoundException("Folha LMC não encontrada: " + folhaId));
        LmcMedicaoTanque novaMedicao = folhaFactory.criarMedicao(
                folha,
                medicaoDTO,
                buscarTanque(medicaoDTO.getTanqueId())
        );
        LmcMedicaoTanque medicaoSalva = medicaoTanqueRepository.save(novaMedicao);
        folha.getMedicoesTanque().add(medicaoSalva);
        atualizarFolha(folha);
        return medicaoSalva;
    }

    @Transactional
    public LmcCompra adicionarCompra(Long folhaId, LmcFolhaRequestDTO.CompraDTO compraDTO) {
        LmcFolha folha = lmcFolhaRepository.findById(folhaId)
                .orElseThrow(() -> new EntityNotFoundException("Folha LMC não encontrada: " + folhaId));
        LmcCompra novaCompra = folhaFactory.criarCompra(
                folha,
                compraDTO,
                buscarTanque(compraDTO.getTanqueDescargaId())
        );
        LmcCompra compraSalva = compraRepository.save(novaCompra);
        folha.getCompras().add(compraSalva);
        atualizarFolha(folha);
        return compraSalva;
    }

    @Transactional
    public LmcVendaBico adicionarVenda(Long folhaId, LmcFolhaRequestDTO.VendaBicoDTO vendaDTO) {
        LmcFolha folha = lmcFolhaRepository.findById(folhaId)
                .orElseThrow(() -> new EntityNotFoundException("Folha LMC não encontrada: " + folhaId));
        LmcVendaBico novaVenda = folhaFactory.criarVenda(
                folha,
                vendaDTO,
                buscarBico(vendaDTO.getBicoId()),
                folhaCalculator.calcularVolumeVendido(
                        vendaDTO.getEncerranteFechamento(),
                        vendaDTO.getEncerranteAbertura(),
                        vendaDTO.getAfericoes()
                )
        );
        LmcVendaBico vendaSalva = vendaBicoRepository.save(novaVenda);
        folha.getVendasBico().add(vendaSalva);
        atualizarFolha(folha);
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

        atualizarFolha(folha);
    }

    @Transactional
    public void deletarCompra(Long id) {
        LmcCompra compra = compraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compra não encontrada: " + id));
        LmcFolha folha = compra.getLmcFolha();

        folha.getCompras().remove(compra);
        compraRepository.delete(compra);

        atualizarFolha(folha);
    }

    @Transactional
    public void deletarVenda(Long id) {
        LmcVendaBico venda = vendaBicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venda não encontrada: " + id));
        LmcFolha folha = venda.getLmcFolha();

        folha.getVendasBico().remove(venda);
        vendaBicoRepository.delete(venda);

        atualizarFolha(folha);
    }

    private void atualizarFolha(LmcFolha folha) {
        folhaCalculator.atualizarTotais(folha);
        folha.setValorAcumuladoMes(calcularValorAcumuladoMes(folha));
        boolean novaFolha = folha.getId() == null;
        validarObservacoes(folha.getVolumeDisponivel(), folha.getPerdasGanhos(), folha.getObservacoes(), novaFolha);
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
