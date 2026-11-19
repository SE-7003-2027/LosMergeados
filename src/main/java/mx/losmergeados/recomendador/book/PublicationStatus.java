package mx.losmergeados.recomendador.book;

/**
 * Estatus de publicación de una obra.
 *
 * <p>Se serializa en JSON con el nombre de la constante.
 */
public enum PublicationStatus {
    /** En publicación: la obra sigue publicándose. */
    ONGOING,
    /** Finalizado: la obra terminó. */
    COMPLETED,
    /** En pausa: publicación detenida temporalmente. */
    HIATUS,
    /** Cancelado: la publicación se canceló. */
    CANCELLED
}
