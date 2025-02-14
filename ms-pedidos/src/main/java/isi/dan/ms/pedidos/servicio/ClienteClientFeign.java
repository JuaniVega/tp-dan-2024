package isi.dan.ms.pedidos.servicio;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import isi.dan.ms.pedidos.dto.ClienteDTO;
import isi.dan.ms.pedidos.dto.ObraDTO;

@FeignClient("MS-CLIENTES")
public interface ClienteClientFeign {
    
    @GetMapping(value="/api/clientes/{id}")
    ClienteDTO getClienteById(@PathVariable("id") Integer id);

    @GetMapping(value="/api/obras/{id}")
    ObraDTO getObraById(@PathVariable("id") Integer id);    
}