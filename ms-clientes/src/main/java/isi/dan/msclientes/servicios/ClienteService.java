package isi.dan.msclientes.servicios;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import isi.dan.msclientes.dao.ClienteRepository;
import isi.dan.msclientes.exception.ClienteNotFoundException;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.UsuarioHabilitado;

@Service
public class ClienteService {

	@Value("${isi.dan.msclientes.default_max_descubierto:1000}")
	private BigDecimal defaultMaximoDescubierto;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private UsuarioHabilitadoService usuarioHabilitadoService;

	public List<Cliente> findAll() {
		return clienteRepository.findAll();
	}

	public Optional<Cliente> findById(Integer id) {
		return clienteRepository.findById(id);
	}

	public Cliente save(Cliente cliente) {
		if (cliente.getMaximoDescubierto() == null) {
			cliente.setMaximoDescubierto(defaultMaximoDescubierto);
		}
		return clienteRepository.save(cliente);
	}

	public Cliente update(Cliente cliente) {
		return clienteRepository.save(cliente);
	}

	public void deleteById(Integer id) {
		clienteRepository.deleteById(id);
	}

	public Cliente addEnabledUser(Integer idCliente, List<Integer> usuariosHabilitadosId)
			throws ClienteNotFoundException {
		Cliente cliente = findById(idCliente)
				.orElseThrow(() -> new ClienteNotFoundException("Cliente " + idCliente + " no encontrado"));

		// Obtenemos usuarios actuales
		List<UsuarioHabilitado> usuariosActuales = cliente.getUsuariosHabilitados();

		// Comparamos y filtramos solo usuarios sin asignar
		List<UsuarioHabilitado> nuevosUsuarios = usuarioHabilitadoService.findAllById(usuariosHabilitadosId).stream()
				.filter(usuario -> !usuariosActuales.contains(usuario)).collect(Collectors.toList());

		// Agregamos nuevos usuarios a los usuarios actuales
		usuariosActuales.addAll(nuevosUsuarios);

		// Actualizamos usuarios habilitados y guardamos
		cliente.setUsuariosHabilitados(usuariosActuales);
		return clienteRepository.save(cliente);
	}
}
