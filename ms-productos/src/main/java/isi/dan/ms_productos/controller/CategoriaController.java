package isi.dan.ms_productos.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import isi.dan.ms_productos.aop.LogExecutionTime;
import isi.dan.ms_productos.exception.CategoriaNotFoundException;
import isi.dan.ms_productos.modelo.Categoria;
import isi.dan.ms_productos.servicio.CategoriaService;

@RestController
@RequestMapping("/api/categoria")
public class CategoriaController {

	@Autowired
	private CategoriaService categoriaService;

	Logger log = LoggerFactory.getLogger(CategoriaController.class);

	@PostMapping
	@LogExecutionTime
	public ResponseEntity<Categoria> saveCategoria(@RequestBody @Validated Categoria categoria) {
		Categoria savedCategoria = categoriaService.saveCategoria(categoria);
		return ResponseEntity.ok(savedCategoria);
	}

	@GetMapping
	@LogExecutionTime
	public List<Categoria> getAllCategorias() {
		return categoriaService.findAllCategorias();
	}

	@GetMapping("/{id}")
	@LogExecutionTime
	public ResponseEntity<Categoria> getCategoriaById(@PathVariable Long id) throws CategoriaNotFoundException {
		return ResponseEntity.ok(categoriaService.findCategoriaById(id));
	}

	@DeleteMapping("/{id}")
	@LogExecutionTime
	public ResponseEntity<Void> deleteCategoria(@PathVariable Long id) {
		categoriaService.deleteCategoriaById(id);
		return ResponseEntity.noContent().build();
	}
}
