package isi.dan.ms_productos.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockUpdateDTO {
	@NotNull(message = "ID must not be null")
	private Long idProducto;
	private Integer cantidad;
	private BigDecimal precio;
	private boolean reponerStock;
}
