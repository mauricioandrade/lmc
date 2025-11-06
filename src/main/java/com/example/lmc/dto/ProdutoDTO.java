package com.example.lmc.dto;

import com.example.lmc.entity.Produto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoDTO {

    private Long id;
    private String nome;

    public static ProdutoDTO fromEntity(Produto produto) {
        return new ProdutoDTO(produto.getId(), produto.getNome());
    }
}
