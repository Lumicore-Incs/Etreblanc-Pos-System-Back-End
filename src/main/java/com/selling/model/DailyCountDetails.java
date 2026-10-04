package com.selling.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(exclude = { "dailyCount", "product" })
@Entity
@Table(name = "daily_count_details")
public class DailyCountDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "product_id")
	private Integer productId;

	@Column(name = "product_name")
	private String productName;

	@Column(name = "category")
	private Integer category;

	@Column(name = "qty")
	private Integer qty;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "daily_count_id", referencedColumnName = "id")
	private DailyCount dailyCount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", referencedColumnName = "product_id", insertable = false, updatable = false)
	private Product product;
}
