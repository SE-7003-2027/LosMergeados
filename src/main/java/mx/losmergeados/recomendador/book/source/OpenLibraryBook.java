package mx.losmergeados.recomendador.book.source;

import java.util.List;

/**
 * Representa una obra obtenida de Open Library.
 *
 * Conserva la clave externa para distinguir la obra
 * de los identificadores internos del catálogo.
 */
public record OpenLibraryBook(
        String sourceId,
        String title,
        List<String> authors,
        List<String> subjects,
        Integer firstPublishYear,
        String coverUrl,
        String sourceUrl
) {
    public OpenLibraryBook {
        authors = authors == null ? List.of() : List.copyOf(authors);
        subjects = subjects == null ? List.of() : List.copyOf(subjects);
    }
}
