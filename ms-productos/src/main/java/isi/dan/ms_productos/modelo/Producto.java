package isi.dan.ms_productos.modelo;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "MS_PRD_PRODUCTO")
@Data
public class Producto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotNull
	private String nombre;
	private String descripcion;
	@Column(name = "STOCK_ACTUAL")
	private int stockActual;
	@Column(name = "STOCK_MINIMO")
	private int stockMinimo;
	private BigDecimal precio;
	private Float descuento;

	@ManyToOne
	@JoinColumn(name = "categoria_id")
	private Categoria categoria;

}
