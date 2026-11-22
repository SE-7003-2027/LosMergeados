package mx.losmergeados.recomendador.rating;

/**
 * Datos necesarios para registrar una valoración.
 *
 * @param bookId identificador del libro
 * @param score puntuación entre 1 y 10
 */
public record RatingRequest(
        Long bookId,
        Integer score
) {}