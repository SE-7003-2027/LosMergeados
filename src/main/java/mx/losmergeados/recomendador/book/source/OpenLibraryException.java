
package mx.losmergeados.recomendador.book.source;

/**
 * Indica que ocurrió un error al consultar Open Library.
 */
public class OpenLibraryException extends RuntimeException {

    public OpenLibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
