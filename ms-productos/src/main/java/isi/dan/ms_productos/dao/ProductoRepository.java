package isi.dan.ms_productos.dao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import isi.dan.ms_productos.modelo.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

	@Query(value = "SELECT * FROM ms_prd_producto p WHERE " + "(:id IS NULL OR p.id = :id) AND "
			+ "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER('%' || :nombre || '%')) AND "
			+ "(:precioMin IS NULL OR p.precio >= :precioMin) AND "
			+ "(:precioMax IS NULL OR p.precio <= :precioMax) AND "
			+ "(:stockMin IS NULL OR p.stock_actual >= :stockMin) AND "
			+ "(:stockMax IS NULL OR p.stock_actual <= :stockMax)", nativeQuery = true)
	List<Producto> buscarProductos(@Param("id") Long id, @Param("nombre") String nombre,
			@Param("precioMin") BigDecimal precioMin, @Param("precioMax") BigDecimal precioMax,
			@Param("stockMin") Integer stockMin, @Param("stockMax") Integer stockMax);

}
