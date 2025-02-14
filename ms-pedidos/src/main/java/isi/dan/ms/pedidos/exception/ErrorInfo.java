package isi.dan.ms.pedidos.exception;

import java.time.Instant;

public record ErrorInfo(Instant fecha, String description, String detalle, Integer codigo) {
}    
