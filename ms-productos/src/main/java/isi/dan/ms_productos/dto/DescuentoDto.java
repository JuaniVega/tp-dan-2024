package isi.dan.ms_productos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DescuentoDto {
	@NotNull(message = "ID must not be null")
	private Long idProducto;
	private Float descuento;
}
