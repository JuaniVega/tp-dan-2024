package isi.dan.msclientes.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import isi.dan.msclientes.aop.LogExecutionTime;
import isi.dan.msclientes.model.Obra;
import isi.dan.msclientes.servicios.ObraService;

@RestController
@RequestMapping("/api/obras")
public class ObraController {

	@Autowired
	private ObraService obraService;

	@GetMapping
	@LogExecutionTime
	public List<Obra> getAll() {
		return obraService.findAll();
	}

	@GetMapping("/{id}")
	@LogExecutionTime
	public ResponseEntity<Obra> getById(@PathVariable Integer id) {
		Optional<Obra> obra = obraService.findById(id);
		return obra.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public Obra create(@RequestBody Obra obra) {
		return obraService.save(obra);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Obra> update(@PathVariable Integer id, @RequestBody Obra obra) {
		if (!obraService.findById(id).isPresent()) {
			return ResponseEntity.notFound().build();
		}
		obra.setId(id);
		return ResponseEntity.ok(obraService.update(obra));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		if (!obraService.findById(id).isPresent()) {
			return ResponseEntity.notFound().build();
		}
		obraService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
