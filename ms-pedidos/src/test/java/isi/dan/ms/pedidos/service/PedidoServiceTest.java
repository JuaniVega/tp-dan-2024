package isi.dan.ms.pedidos.service;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;

import feign.FeignException;
import isi.dan.ms.pedidos.dao.PedidoRepository;
import isi.dan.ms.pedidos.servicio.PedidoService;

@SpringBootTest
public class PedidoServiceTest {
    
    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    public void testSavePedido() throws FeignException, JsonProcessingException {
        
    }

}
