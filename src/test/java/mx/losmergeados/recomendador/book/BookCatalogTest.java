package mx.losmergeados.recomendador.book;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias del catálogo en memoria (sin levantar Spring).
 */
class BookCatalogTest {

    private final BookCatalog catalog = new BookCatalog();

    @Test
    void sinFiltros_devuelveTodoElCatalogo() {
        assertThat(catalog.list(null, null)).hasSize(11);
    }

    @Test
    void losIdsSonUnicos() {
        List<Book> books = catalog.list(null, null);
        assertThat(new HashSet<>(books.stream().map(Book::id).toList())).hasSize(books.size());
    }

    @Test
    void filtraPorFormato() {
        assertThat(catalog.list(Format.MANGA, null))
                .isNotEmpty()
                .allMatch(book -> book.format() == Format.MANGA);
    }

    @Test
    void combinaFormatoYEstatus() {
        assertThat(catalog.list(Format.MANGA, PublicationStatus.ONGOING))
                .extracting(Book::title)
                .containsExactly("Berserk", "One Piece");
    }

    @Test
    void findById_existenteYNo() {
        assertThat(catalog.findById(3)).map(Book::title).contains("Watchmen");
        assertThat(catalog.findById(999)).isEmpty();
    }
}
