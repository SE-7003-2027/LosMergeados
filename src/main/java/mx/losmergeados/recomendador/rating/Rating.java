package mx.losmergeados.recomendador.rating;

/**
 * Representa la información pública de una valoración.
 *
 * @param id identificador de la valoración
 * @param userId identificador del usuario
 * @param bookId identificador del libro
 * @param score puntuación otorgada
 */
public record Rating(
        Long id,
        Long userId,
        Long bookId,
        Integer score
) {}