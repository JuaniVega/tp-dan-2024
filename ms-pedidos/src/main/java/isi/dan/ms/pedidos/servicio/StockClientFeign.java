package isi.dan.ms.pedidos.servicio;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("MS-PRODUCTOS")
public interface StockClientFeign {
    @GetMapping(value="/api/productos/{id}/stock-suficiente")
    Boolean verificarStockSuficiente(@PathVariable("id") Long id, @RequestParam("cantidad") Integer cantidad);
}
