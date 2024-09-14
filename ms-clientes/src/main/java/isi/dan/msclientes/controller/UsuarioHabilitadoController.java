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
import isi.dan.msclientes.model.UsuarioHabilitado;
import isi.dan.msclientes.servicios.UsuarioHabilitadoService;

@RestController
@RequestMapping("/api/usuario-habilitado")
public class UsuarioHabilitadoController {

	@Autowired
	private UsuarioHabilitadoService usuarioHabilitadoService;

	@GetMapping
	@LogExecutionTime
	public List<UsuarioHabilitado> getAll() {
		return usuarioHabilitadoService.findAll();
	}

	@GetMapping("/{id}")
	@LogExecutionTime
	public ResponseEntity<UsuarioHabilitado> getById(@PathVariable Integer id) {
		Optional<UsuarioHabilitado> usuario = usuarioHabilitadoService.findById(id);
		return usuario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public UsuarioHabilitado create(@RequestBody UsuarioHabilitado usuario) {
		return usuarioHabilitadoService.save(usuario);
	}

	@PutMapping("/{id}")
	public ResponseEntity<UsuarioHabilitado> update(@PathVariable Integer id, @RequestBody UsuarioHabilitado usuario) {
		if (!usuarioHabilitadoService.findById(id).isPresent()) {
			return ResponseEntity.notFound().build();
		}
		usuario.setId(id);
		return ResponseEntity.ok(usuarioHabilitadoService.update(usuario));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		if (!usuarioHabilitadoService.findById(id).isPresent()) {
			return ResponseEntity.notFound().build();
		}
		usuarioHabilitadoService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
