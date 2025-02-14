package isi.dan.ms.pedidos.mapper;

import isi.dan.ms.pedidos.modelo.Cliente;

import org.springframework.stereotype.Component;

import isi.dan.ms.pedidos.dto.ClienteDTO;

@Component
public class ClienteMapper {
    
    public Cliente transformarDTOEnCliente(ClienteDTO clienteDTO){
        Cliente cliente = new Cliente();
        cliente.setId(clienteDTO.getId());
        cliente.setNombre(clienteDTO.getNombre());
        cliente.setCorreoElectronico(clienteDTO.getCorreoElectronico());
        cliente.setCuit(clienteDTO.getCuit());

        return cliente;
    }

}
