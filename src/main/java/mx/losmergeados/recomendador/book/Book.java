package mx.losmergeados.recomendador.book;

/**
 * Representa una obra del catálogo (libro, cómic o manga).
 *
 * <p>Por ahora es un {@code record} inmutable sin persistencia. Cuando se
 * agregue la base de datos se convertirá en entidad JPA.
 *
 * @param id     identificador único
 * @param title  título de la obra
 * @param author nombre del autor o autora
 * @param genre  género principal (ej. "Fantasía", "Thriller", "Superhéroes")
 * @param format formato de la obra
 * @param status estatus de publicación
 */
public record Book(
        Long id,
        String title,
        String author,
        String genre,
        Format format,
        PublicationStatus status
) {}
