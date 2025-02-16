package isi.dan.ms.pedidos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Ignora cualquier campo no definido en la clase
public class StockUpdateDTO {
    private Long idProducto;
    private Integer cantidad;
    private boolean reponerStock;
}
