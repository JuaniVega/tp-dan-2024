package isi.dan.msclientes.servicios;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import isi.dan.msclientes.dao.UsuarioHabilitadoRepository;
import isi.dan.msclientes.model.UsuarioHabilitado;

@Service
public class UsuarioHabilitadoService {

	@Autowired
	private UsuarioHabilitadoRepository usuarioHabilitadoRepository;

	Logger logger = LoggerFactory.getLogger(ObraService.class);

	public List<UsuarioHabilitado> findAll() {
		return usuarioHabilitadoRepository.findAll();
	}

	public Optional<UsuarioHabilitado> findById(Integer id) {
		return usuarioHabilitadoRepository.findById(id);
	}

	public UsuarioHabilitado save(UsuarioHabilitado usuario) {
		logger.info("Guardando usuario habilitado.");
		return usuarioHabilitadoRepository.save(usuario);
	}

	public UsuarioHabilitado update(UsuarioHabilitado usuario) {
		logger.info("Actualizando usuario habilitado.");
		return usuarioHabilitadoRepository.save(usuario);
	}

	public void deleteById(Integer id) {
		logger.info("Eliminando usuario habilitado. Id: {} ", id);
		usuarioHabilitadoRepository.deleteById(id);
	}

	public List<UsuarioHabilitado> findAllById(List<Integer> usuariosHabilitadosId) {
		return usuarioHabilitadoRepository.findAllById(usuariosHabilitadosId);
	}

}
