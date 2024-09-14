package isi.dan.msclientes.servicios;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import isi.dan.msclientes.dao.ObraRepository;
import isi.dan.msclientes.exception.ClienteNotFoundException;
import isi.dan.msclientes.exception.ObraNotFoundException;
import isi.dan.msclientes.exception.StateErrorException;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.EstadoObraEnum;
import isi.dan.msclientes.model.Obra;
import jakarta.transaction.Transactional;

@Service
public class ObraService {

	Logger logger = LoggerFactory.getLogger(ObraService.class);

	@Autowired
	private ClienteService clienteService;

	@Autowired
	private ObraRepository obraRepository;

	public List<Obra> findAll() {
		return obraRepository.findAll();
	}

	public Optional<Obra> findById(Integer id) {
		return obraRepository.findById(id);
	}

	public Obra save(Obra obra) {
		obra.setEstado(validarHabilitacionUsuario(obra.getCliente(), obra) ? EstadoObraEnum.HABILITADA
				: EstadoObraEnum.PENDIENTE);
		logger.info("Guardando obra. Id: {}. Estado: {}", obra.getId(), obra.getEstado());
		return obraRepository.save(obra);
	}

	@Transactional
	public Obra update(Obra obra) {
		if (findById(obra.getId()).isPresent()) {

			// Obtiene el estado anterior al actualizado
			EstadoObraEnum estadoPrevio = findById(obra.getId()).get().getEstado();

			try {
				// Verifica si el cambio de estados que se quiere hacer es correcto
				if (validarEstadoPrevio(obra.getEstado(), estadoPrevio)) {

					if (obra.getCliente() != null) {
						if (obra.getEstado() == EstadoObraEnum.FINALIZADA) {
							finalizarObra(obra);

						} else if (obra.getEstado() == EstadoObraEnum.HABILITADA
								&& !validarHabilitacionUsuario(obra.getCliente(), obra)) {
							obra.setEstado(EstadoObraEnum.PENDIENTE);

						} else if (obra.getEstado() == EstadoObraEnum.PENDIENTE
								&& validarHabilitacionUsuario(obra.getCliente(), obra)) {
							obra.setEstado(EstadoObraEnum.HABILITADA);
						}
					}
				}
			} catch (StateErrorException e) {
				// Si el cambio de estados no es correcto, deja el valor anterior
				obra.setEstado(estadoPrevio);
			}
		}
		logger.info("Actualizando obra. Id: {}. Estado: {}", obra.getId(), obra.getEstado());
		return obraRepository.save(obra);
	}

	private boolean validarEstadoPrevio(EstadoObraEnum estadoActual, EstadoObraEnum estadoPrevio)
			throws StateErrorException {
		if (estadoActual == EstadoObraEnum.HABILITADA && estadoPrevio == EstadoObraEnum.FINALIZADA) {
			logger.error("No se puede habilitar una obra finalizada");
			throw new StateErrorException("No se puede habilitar una obra finalizada");
		}
		if (estadoActual == EstadoObraEnum.PENDIENTE && estadoPrevio == EstadoObraEnum.FINALIZADA) {
			logger.error("No se puede pasar a estado pendiente una obra finalizada");
			throw new StateErrorException("No se puede pasar a estado pendiente una obra finalizada");
		}
		if (estadoActual == EstadoObraEnum.FINALIZADA && estadoPrevio == EstadoObraEnum.PENDIENTE) {
			logger.error("No se puede finalizar una obra en estado pendiente");
			throw new StateErrorException("No se puede finalizar una obra en estado pendiente");
		}
		return true;
	}

	@Transactional
	private void finalizarObra(Obra obra) {
		List<Obra> obrasPendientes = obraRepository.findByClienteIdAndEstado(obra.getCliente().getId(),
				EstadoObraEnum.PENDIENTE);
		if (obrasPendientes.size() > 0) {
			Obra obraActualizada = obrasPendientes.get(0);
			obraActualizada.setEstado(EstadoObraEnum.HABILITADA);
			obraRepository.save(obraActualizada);
		}

		logger.info("Habilitando obra pendiente. Id: {}", obra.getId());
	}

	public void deleteById(Integer id) {
		logger.info("Eliminando obra. Id: {} ", id);
		obraRepository.deleteById(id);
	}

	@Transactional
	public void asignarCliente(Integer idCliente, Integer idObra)
			throws ClienteNotFoundException, ObraNotFoundException, StateErrorException {
		Cliente cliente = clienteService.findById(idCliente)
				.orElseThrow(() -> new ClienteNotFoundException("No se encontró un cliente con el id: " + idCliente));
		Obra obra = findById(idObra)
				.orElseThrow(() -> new ObraNotFoundException("No se encontró una obra con el id: " + idObra));

		obra.setCliente(cliente);
		obra.setEstado(
				validarHabilitacionUsuario(cliente, obra) ? EstadoObraEnum.HABILITADA : EstadoObraEnum.PENDIENTE);

		logger.info("Asignando cliente {} a la obra {}", idCliente, idObra);
		update(obra);
	}

	private boolean validarHabilitacionUsuario(Cliente cliente, Obra obra) {
		Integer obrasEnEjecucion = obraRepository.countByClienteIdAndEstado(cliente.getId(), EstadoObraEnum.HABILITADA);
		return obrasEnEjecucion < cliente.getMaxObrasEjecucion();
	}
}
