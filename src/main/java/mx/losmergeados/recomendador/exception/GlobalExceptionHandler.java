package mx.losmergeados.recomendador.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import mx.losmergeados.recomendador.book.source.OpenLibraryException;

/**
 * Maneja errores de servicios externos utilizados por la API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OpenLibraryException.class)
    public ProblemDetail handleOpenLibraryException(
            OpenLibraryException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                "No fue posible consultar Open Library."
        );

        problem.setTitle("Error en el servicio externo");

        return problem;
    }
}
