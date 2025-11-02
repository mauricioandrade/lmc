package com.example.lmc.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table (name = "tb_bico")
public class Bico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false, unique = true)
    private String numero;

    @ManyToOne
    @JoinColumn(name = "tanque_id", nullable = false)
    @JsonBackReference("tanque-bico")
    private Tanque tanque;


}
