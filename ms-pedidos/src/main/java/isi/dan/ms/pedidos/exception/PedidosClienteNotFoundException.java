package isi.dan.ms.pedidos.exception;

public class PedidosClienteNotFoundException extends Exception{
    public PedidosClienteNotFoundException(Integer id){
        super("No se encontraron pedidos para el cliente "+id);
    }
}
