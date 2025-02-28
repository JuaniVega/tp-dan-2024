package isi.dan.ms.pedidos.modelo;

public enum EstadoPedido {
    RECIBIDO, ACEPTADO, RECHAZADO, EN_PREPARACION, ENTREGADO, CANCELADO;

    public static EstadoPedido fromString(String estado) {
        
        if (estado == null || estado.isEmpty()) {
            return null;
        }

        for (EstadoPedido e : EstadoPedido.values()) {
            if (e.name().equalsIgnoreCase(estado)) {
                return e;
            }
        }
        throw new IllegalArgumentException("EstadoPedido inválido: " + estado);
    }
}

