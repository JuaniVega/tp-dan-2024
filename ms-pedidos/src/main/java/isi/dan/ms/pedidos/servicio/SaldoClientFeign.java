package isi.dan.ms.pedidos.servicio;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("MS-CLIENTES")
public interface SaldoClientFeign {
    
    @GetMapping(value="/api/clientes/{id}/saldo-suficiente")
    Boolean verificarSaldoSuficiente(@PathVariable("id") Integer id, @RequestParam("total") BigDecimal total);
}
