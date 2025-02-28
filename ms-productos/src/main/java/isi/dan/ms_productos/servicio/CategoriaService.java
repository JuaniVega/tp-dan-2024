package isi.dan.ms_productos.servicio;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import isi.dan.ms_productos.dao.CategoriaRepository;
import isi.dan.ms_productos.exception.CategoriaNotFoundException;
import isi.dan.ms_productos.modelo.Categoria;

@Service
public class CategoriaService {

	@Autowired
	private CategoriaRepository repository;
	Logger log = LoggerFactory.getLogger(CategoriaService.class);

	public Categoria saveCategoria(Categoria categoria) {
		log.info("Guardando categoría con nombre: {}", categoria.getNombre());
		return repository.save(categoria);
	}

	public List<Categoria> findAllCategorias() {
		log.info("Obteniendo todas las categorías.");
		return repository.findAll();
	}

	public Categoria findCategoriaById(Long id) throws CategoriaNotFoundException {
		log.info("Buscando categoría con id: {}", id);
		return repository.findById(id).orElseThrow(() -> {
			log.error("No se encontró categoría con id: {}", id);
			return new CategoriaNotFoundException(id);
		});
	}

	public void deleteCategoriaById(Long id) {
		log.info("Eliminando categoría con id: {}", id);
		repository.deleteById(id);
	}
}
