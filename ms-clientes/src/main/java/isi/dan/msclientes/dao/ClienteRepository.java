package isi.dan.msclientes.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import isi.dan.msclientes.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

	@Query(value = "SELECT * FROM ms_cli_cliente c WHERE "
			+ "(:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND "
			+ "(:correoElectronico IS NULL OR LOWER(c.correo_electronico) LIKE LOWER(CONCAT('%', :correoElectronico, '%'))) AND "
			+ "(:cuit IS NULL OR LOWER(c.cuit) LIKE LOWER(CONCAT('%', :cuit, '%')))", nativeQuery = true)
	List<Cliente> buscarClientes(@Param("nombre") String nombre, @Param("correoElectronico") String correoElectronico,
			@Param("cuit") String cuit);

}
