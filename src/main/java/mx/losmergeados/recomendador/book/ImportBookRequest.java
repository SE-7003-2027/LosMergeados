
package mx.losmergeados.recomendador.book;

import mx.losmergeados.recomendador.book.source.OpenLibraryBook;

public record ImportBookRequest(
        OpenLibraryBook book,
        Format format,
        PublicationStatus status
) {
}
