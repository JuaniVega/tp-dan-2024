package isi.dan.ms.pedidos.mapper;

import org.springframework.stereotype.Component;

import isi.dan.ms.pedidos.dto.ProductoDTO;
import isi.dan.ms.pedidos.modelo.Producto;

@Component
public class ProductoMapper {
    
    public Producto transformarDTOEnProducto(ProductoDTO productoDTO){
        Producto producto = new Producto();
        producto.setId(productoDTO.getId());
        producto.setNombre(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        return producto;
    }

}
