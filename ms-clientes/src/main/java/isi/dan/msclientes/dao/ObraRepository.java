package isi.dan.msclientes.dao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import isi.dan.msclientes.model.EstadoObraEnum;
import isi.dan.msclientes.model.Obra;

@Repository
public interface ObraRepository extends JpaRepository<Obra, Integer> {

    List<Obra> findByPresupuestoGreaterThanEqual(BigDecimal price);

	Integer countByClienteIdAndEstado(Integer id, EstadoObraEnum habilitada);

	List<Obra> findByClienteIdAndEstado(Integer id, EstadoObraEnum pendiente);
}

