package com.example.lmc.dto;

import com.example.lmc.entity.Empresa;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmpresaDTO {

    private Long id;
    private String razaoSocial;
    private String cnpj;
    private String inscricaoEstadual;
    private String enderecoCompleto;
    private Boolean isAtiva;

    public EmpresaDTO(Empresa empresa) {
        this.id = empresa.getId();
        this.razaoSocial = empresa.getRazaoSocial();
        this.cnpj = empresa.getCnpj();
        this.inscricaoEstadual = empresa.getInscricaoEstadual();
        this.enderecoCompleto = empresa.getEnderecoCompleto();
        this.isAtiva = empresa.getIsAtiva();
    }
}
