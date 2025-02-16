package isi.dan.ms_productos.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;

import isi.dan.ms_productos.dao.CategoriaRepository;
import isi.dan.ms_productos.dao.ProductoRepository;
import isi.dan.ms_productos.dto.DescuentoDto;
import isi.dan.ms_productos.dto.StockUpdateDTO;
import isi.dan.ms_productos.exception.CategoriaNotFoundException;
import isi.dan.ms_productos.exception.ProductoNotFoundException;
import isi.dan.ms_productos.modelo.Categoria;
import isi.dan.ms_productos.modelo.Producto;
import isi.dan.ms_productos.servicio.CategoriaService;
import isi.dan.ms_productos.servicio.ProductoService;

@SpringBootTest
public class ProductoServiceTest {

	@Mock
	private ProductoRepository productoRepository;

	@Mock
	private CategoriaRepository categoriaRepository;

	@InjectMocks
	private ProductoService productoService;

	@Mock
	private CategoriaService categoriaService;

	@Test
	public void testSaveProducto() throws CategoriaNotFoundException {
		Producto productoMock = new Producto();
		productoMock.setDescuento(null);

		Categoria categoriaMock = new Categoria();
		categoriaMock.setId(1L);
		categoriaMock.setNombre("Categoria 1");

		productoMock.setCategoria(categoriaMock);

		when(categoriaRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(categoriaMock));
		when(productoRepository.save(Mockito.any(Producto.class))).thenReturn(productoMock);
		when(categoriaService.findCategoriaById(categoriaMock.getId())).thenReturn(categoriaMock);

		Producto result = productoService.saveProducto(productoMock);

		assertEquals(Float.valueOf(0), result.getDescuento());
		assertEquals(0, result.getStockActual());
		assertEquals(categoriaMock, result.getCategoria());
		verify(categoriaService).findCategoriaById(categoriaMock.getId());
		verify(productoRepository).save(productoMock);
	}

	@Test
	public void testGetProductoById() throws ProductoNotFoundException {
		Long productId = 1L;
		Producto productoMock = new Producto();
		productoMock.setId(productId);

		when(productoRepository.findById(productId)).thenReturn(Optional.of(productoMock));

		Producto result = productoService.getProductoById(productId);

		assertEquals(productId, result.getId());
		verify(productoRepository).findById(productId);
	}

	@Test
	public void testGetProductoById_ProductoNotFoundException() {
		Long productId = 1L;

		when(productoRepository.findById(productId)).thenReturn(Optional.empty());

		assertThrows(ProductoNotFoundException.class, () -> productoService.getProductoById(productId));
	}

	@Test
	public void testDeleteProducto() {
		Long productId = 1L;

		productoService.deleteProducto(productId);

		verify(productoRepository).deleteById(productId);
	}

	@Test
	public void testPutOrdenProvision() throws ProductoNotFoundException, JsonProcessingException {
		StockUpdateDTO ordenProvision = new StockUpdateDTO();
		ordenProvision.setIdProducto(1L);
		ordenProvision.setCantidad(10);
		ordenProvision.setPrecio(BigDecimal.valueOf(50));

		Producto productoMock = new Producto();
		productoMock.setStockActual(20);

		when(productoRepository.findById(ordenProvision.getIdProducto())).thenReturn(Optional.of(productoMock));
		when(productoRepository.save(productoMock)).thenReturn(productoMock);

		Producto result = productoService.putOrdenProvision(ordenProvision);

		assertEquals(30, result.getStockActual());
		assertEquals(BigDecimal.valueOf(50), result.getPrecio());
		verify(productoRepository).save(productoMock);
	}

	@Test
	public void testPutOrdenProvision_ProductoNotFoundException() {
		StockUpdateDTO ordenProvision = new StockUpdateDTO();
		ordenProvision.setIdProducto(1L);

		when(productoRepository.findById(ordenProvision.getIdProducto())).thenReturn(Optional.empty());

		assertThrows(ProductoNotFoundException.class, () -> productoService.putOrdenProvision(ordenProvision));
	}

	@Test
	public void testUpdateDescuento() throws ProductoNotFoundException {
		DescuentoDto descuentoDto = new DescuentoDto();
		descuentoDto.setIdProducto(1L);
		descuentoDto.setDescuento(10f);

		Producto productoMock = new Producto();

		when(productoRepository.findById(descuentoDto.getIdProducto())).thenReturn(Optional.of(productoMock));
		when(productoRepository.save(productoMock)).thenReturn(productoMock);

		Producto result = productoService.updateDescuento(descuentoDto);

		assertEquals(Float.valueOf(10), result.getDescuento());
		verify(productoRepository).save(productoMock);
	}

	@Test
	public void testUpdateDescuento_ProductoNotFoundException() {
		DescuentoDto descuentoDto = new DescuentoDto();
		descuentoDto.setIdProducto(1L);

		when(productoRepository.findById(descuentoDto.getIdProducto())).thenReturn(Optional.empty());

		assertThrows(ProductoNotFoundException.class, () -> productoService.updateDescuento(descuentoDto));
	}
}
