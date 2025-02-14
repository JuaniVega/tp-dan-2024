package isi.dan.ms.pedidos.exception;

public class ObraConDistintoClienteAsignadoException extends Exception {
    public ObraConDistintoClienteAsignadoException(Integer idCliente, Integer idClienteObra){
        super("La obra no tiene asignado al cliente "+idCliente+", sino al cliente "+idClienteObra);
    }
}
