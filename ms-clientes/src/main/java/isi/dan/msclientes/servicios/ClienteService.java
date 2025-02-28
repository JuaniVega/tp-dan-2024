package isi.dan.msclientes.servicios;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import isi.dan.msclientes.dao.ClienteRepository;
import isi.dan.msclientes.exception.ClienteNotFoundException;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.UsuarioHabilitado;

@Service
public class ClienteService {
    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);

	@Value("${isi.dan.msclientes.default_max_descubierto:1000}")
	private BigDecimal defaultMaximoDescubierto;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private UsuarioHabilitadoService usuarioHabilitadoService;

	public List<Cliente> findAll() {
        logger.info("Buscando todos los clientes");
		return clienteRepository.findAll();
	}

	public Optional<Cliente> findById(Integer id) {
        logger.debug("Buscando cliente con ID: {}", id);
		return clienteRepository.findById(id);
	}

	public Cliente save(Cliente cliente) {
		if (cliente.getMaximoDescubierto() == null) {
			cliente.setMaximoDescubierto(defaultMaximoDescubierto);
            logger.debug("Asignado maximo descubierto por defecto: {}", defaultMaximoDescubierto);
		}
        Cliente savedCliente = clienteRepository.save(cliente);
        logger.info("Cliente guardado con ID: {}", savedCliente.getId());
        return savedCliente;
	}

	public Cliente update(Cliente cliente) {
        logger.info("Actualizando cliente con ID: {}", cliente.getId());
		return clienteRepository.save(cliente);
	}

	public void deleteById(Integer id) {
        logger.warn("Eliminando cliente con ID: {}", id);
		clienteRepository.deleteById(id);
	}

	public Cliente addEnabledUser(Integer idCliente, List<Integer> usuariosHabilitadosId)
			throws ClienteNotFoundException {
        logger.info("Agregando usuarios habilitados {} al cliente {}", usuariosHabilitadosId, idCliente);

		Cliente cliente = findById(idCliente)
				.orElseThrow(() -> {
                    logger.error("Cliente {} no encontrado", idCliente);
                    return new ClienteNotFoundException("Cliente " + idCliente + " no encontrado");
                });

		// Obtenemos usuarios actuales
		List<UsuarioHabilitado> usuariosActuales = cliente.getUsuariosHabilitados();

		// Comparamos y filtramos solo usuarios sin asignar
		List<UsuarioHabilitado> nuevosUsuarios = usuarioHabilitadoService.findAllById(usuariosHabilitadosId).stream()
				.filter(usuario -> !usuariosActuales.contains(usuario)).collect(Collectors.toList());

        if (nuevosUsuarios.isEmpty()) {
            logger.warn("No hay usuarios nuevos para agregar al cliente {}", idCliente);
        }
		// Agregamos nuevos usuarios a los usuarios actuales
		usuariosActuales.addAll(nuevosUsuarios);

		// Actualizamos usuarios habilitados y guardamos
		cliente.setUsuariosHabilitados(usuariosActuales);

        Cliente updatedCliente = clienteRepository.save(cliente);
        logger.info("Usuarios habilitados actualizados para cliente {}", idCliente);
        return updatedCliente;
	}

	public boolean verificarSaldo(Integer idCliente, BigDecimal totalPedido) throws ClienteNotFoundException {
		logger.debug("Verificando saldo para el cliente {} con total de pedido {}", idCliente, totalPedido);

		Cliente cliente = clienteRepository.findById(idCliente)
				.orElseThrow(() -> {
                    logger.error("Cliente {} no encontrado", idCliente);
                    return new ClienteNotFoundException("Cliente " + idCliente + " no encontrado");
                });

        boolean resultado = cliente.getMaximoDescubierto().compareTo(totalPedido) >= 0;
        logger.info("Cliente {} - Saldo verificado: {}", idCliente, resultado);
        return resultado;
	}

    public List<Cliente> buscarClientes(String nombre, String correoElectronico, String cuit) {
    	logger.debug("Buscando clientes con nombre={}, correo={}, cuit={}", nombre, correoElectronico, cuit);
        return clienteRepository.buscarClientes(nombre, correoElectronico, cuit);
    }
}
