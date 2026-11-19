package mx.losmergeados.recomendador.book;

/**
 * Formato (tipo) de una obra del catálogo.
 *
 * <p>Se serializa en JSON con el nombre de la constante: {@code "BOOK"},
 * {@code "COMIC"} o {@code "MANGA"}.
 */
public enum Format {
    /** Libro. */
    BOOK,
    /** Cómic. */
    COMIC,
    /** Manga. */
    MANGA
}
