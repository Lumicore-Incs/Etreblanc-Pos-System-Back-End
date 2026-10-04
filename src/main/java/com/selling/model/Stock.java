package com.selling.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "stockDetails")
@Data
@Entity
@Table(name = "stock")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Integer stock_id;
    private String type;
    private int totalQuantity;
    private String status;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
    private List<StockDetails> stockDetails;
}

