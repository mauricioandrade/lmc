package com.example.lmc.service;

import com.example.lmc.entity.Produto;
import com.example.lmc.exception.BusinessException;
import com.example.lmc.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com id: " + id));
    }

    public Produto salvarProduto(Produto produto) {
        if (produtoRepository.findByNome(produto.getNome()).isPresent()) {
            throw new BusinessException("Já existe um produto com o nome: " + produto.getNome());
        }
        return produtoRepository.save(produto);
    }

    public Produto atualizarProduto(Long id, Produto produtoAtualizado) {
        Produto produtoExistente = buscarPorId(id);

        produtoRepository.findByNome(produtoAtualizado.getNome()).ifPresent(produtoComMesmoNome -> {
            if (!produtoComMesmoNome.getId().equals(id)) {
                throw new BusinessException("Já existe outro produto com o nome: " + produtoAtualizado.getNome());
            }
        });

        produtoExistente.setNome(produtoAtualizado.getNome());

        return produtoRepository.save(produtoExistente);
    }

    public void deletarProduto(Long id) {
        Produto produto = buscarPorId(id);

        produtoRepository.delete(produto);
    }
}

