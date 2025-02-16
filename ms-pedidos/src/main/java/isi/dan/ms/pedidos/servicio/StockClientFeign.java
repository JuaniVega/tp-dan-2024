package isi.dan.ms.pedidos.servicio;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("MS-PRODUCTOS")
public interface StockClientFeign {
    @PostMapping(value="/api/productos/stock-suficiente")
    Boolean verificarStockSuficiente(@RequestBody Map<Long, Integer> productsToCheck);
}
