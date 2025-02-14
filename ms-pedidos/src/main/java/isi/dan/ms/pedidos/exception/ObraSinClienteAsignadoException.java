package isi.dan.ms.pedidos.exception;

public class ObraSinClienteAsignadoException extends Exception {
    public ObraSinClienteAsignadoException(){
        super("La obra no tiene un cliente asignado");
    }
}
