package isi.dan.ms.pedidos.servicio;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import isi.dan.ms.pedidos.dto.ProductoDTO;

@FeignClient("MS-PRODUCTOS")
public interface ProductoClientFeign {
    
    @GetMapping(value="/api/productos/{id}")
    ProductoDTO getProductoById(@PathVariable("id") Long id);
 
}
