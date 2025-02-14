package isi.dan.ms_productos.exception;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestControllerException {

    private static final Logger logger = LoggerFactory.getLogger(RestControllerException.class);

    @ExceptionHandler(ProductoNotFoundException.class)
    public ResponseEntity<ErrorInfo> handleProductNotFoundException(ProductoNotFoundException ex) {
        logger.error("ERROR Buscando Producto", ex);
        String detalle = ex.getCause() == null ? "Producto no encontrado": ex.getCause().getMessage();

        return new ResponseEntity<ErrorInfo>(new ErrorInfo(Instant.now(),ex.getMessage(),detalle,HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CategoriaNotFoundException.class)
    public ResponseEntity<ErrorInfo> handleCategoriaNotFoundException(CategoriaNotFoundException ex) {
        logger.error("ERROR Buscando Categoría", ex);
        String detalle = ex.getCause() == null ? "Categoría no encontrada": ex.getCause().getMessage();

        return new ResponseEntity<ErrorInfo>(new ErrorInfo(Instant.now(),ex.getMessage(),detalle,HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorInfo> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.error("ERROR de Validación", ex);
        String detalle = ex.getCause() == null ? "Argumentos no válidos" : ex.getCause().getMessage();

        return new ResponseEntity<>(new ErrorInfo(Instant.now(), ex.getMessage(), detalle, HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorInfo> handleOtherExceptions(Exception ex) {
        logger.error("ERROR MS CLIENTES", ex);
        String detalle = ex.getCause() == null ? "Error en producto": ex.getCause().getMessage();
        return new ResponseEntity<ErrorInfo>(new ErrorInfo(Instant.now(),ex.getMessage(),detalle ,HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
