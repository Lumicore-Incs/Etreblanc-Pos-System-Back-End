package com.selling.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
@Entity
@Table(name = "daily_count")
public class DailyCount {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "date")
	private Date date;

	@Column(name = "total_qty")
	private Integer totalQty;

	@Column(name = "last_time")
	private LocalDateTime lastTime;

	@OneToMany(mappedBy = "dailyCount", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<DailyCountDetails> dailyCountDetails;
}
