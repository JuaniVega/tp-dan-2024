package isi.dan.ms.pedidos.exception;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import feign.FeignException;

@ControllerAdvice
public class RestExceptionHandler{

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(PedidoNotFoundException.class)
    public ResponseEntity<ErrorInfo> handlePedidoNotFound(PedidoNotFoundException ex) {
        logger.error("ERROR buscando pedido", ex);
        ErrorInfo error = new ErrorInfo(
            Instant.now(),
            "Error al buscar el pedido",
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ErrorInfo> handleFeignNotFound(FeignException.NotFound ex) {
        logger.error("ERROR consultando a otro servicio", ex);
        ErrorInfo error = new ErrorInfo(
            Instant.now(),
            "Error al consultar otro servicio",
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorInfo> handleFeignOtherErrors(FeignException ex) {
        logger.error("ERROR comunicándose con otro servicio", ex);
        ErrorInfo error = new ErrorInfo(
            Instant.now(),
            "Error en la comunicación con otro servicio",
            ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(ObraSinClienteAsignadoException.class)
    public ResponseEntity<ErrorInfo> handleClienteNoAsignado(ObraSinClienteAsignadoException ex) {
        logger.error("ERROR con la obra", ex);
        ErrorInfo error = new ErrorInfo(
            Instant.now(),
            "Error de validación",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ObraConDistintoClienteAsignadoException.class)
    public ResponseEntity<ErrorInfo> handleClienteObraMismatch(ObraConDistintoClienteAsignadoException ex) {
        logger.error("ERROR con la obra", ex);
        ErrorInfo error = new ErrorInfo(
            Instant.now(),
            "Error de validación",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}
