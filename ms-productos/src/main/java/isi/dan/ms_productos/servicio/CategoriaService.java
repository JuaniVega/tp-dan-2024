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
		return repository.save(categoria);
	}

	public List<Categoria> findAllCategorias() {
		return repository.findAll();
	}

	public Categoria findCategoriaById(Long id) throws CategoriaNotFoundException {
		return repository.findById(id).orElseThrow(() -> new CategoriaNotFoundException(id));
	}

	public void deleteCategoriaById(Long id) {
		repository.deleteById(id);
	}
}
