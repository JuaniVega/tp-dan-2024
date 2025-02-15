package isi.dan.ms.pedidos.exception;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.core.JsonProcessingException;

import feign.FeignException;

@ControllerAdvice
public class RestControllerException {

    private static final Logger logger = LoggerFactory.getLogger(RestControllerException.class);

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

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorInfo> handleJsonProcessingException(JsonProcessingException ex){
        logger.error("ERROR en mensaje RabbitMQ", ex);
        ErrorInfo error = new ErrorInfo(
            Instant.now(),
            "Error en el mensaje enviado con RabbitMQ",
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorInfo> handleOtherExceptions(Exception ex) {
        logger.error("ERROR en pedido", ex);
        String detalle = ex.getCause() == null ? "Error en pedido": ex.getCause().getMessage();
        return new ResponseEntity<ErrorInfo>(new ErrorInfo(Instant.now(),ex.getMessage(),detalle ,HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
