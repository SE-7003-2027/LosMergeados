
package mx.losmergeados.recomendador.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class BookCatalogTest {

    @Mock
    private BookRepository repository;

    private BookCatalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new BookCatalog(repository);

        List<BookEntity> books = sampleBooks();

        when(repository.findAll()).thenReturn(books);
    }

    private List<BookEntity> sampleBooks() {
        return List.of(
            entity(1L, "El nombre del viento",
                "Patrick Rothfuss", "Fantasía",
                Format.BOOK, PublicationStatus.ONGOING),

            entity(2L, "Akira",
                "Katsuhiro Otomo", "Ciencia ficción",
                Format.MANGA, PublicationStatus.COMPLETED),

            entity(3L, "Watchmen",
                "Alan Moore", "Superhéroes",
                Format.COMIC, PublicationStatus.COMPLETED),

            entity(4L, "Dune",
                "Frank Herbert", "Ciencia ficción",
                Format.BOOK, PublicationStatus.COMPLETED),

            entity(5L, "Berserk",
                "Kentaro Miura", "Fantasía oscura",
                Format.MANGA, PublicationStatus.ONGOING),

            entity(6L, "Saga",
                "Brian K. Vaughan", "Ciencia ficción",
                Format.COMIC, PublicationStatus.HIATUS),

            entity(7L, "La sombra del viento",
                "Carlos Ruiz Zafón", "Misterio",
                Format.BOOK, PublicationStatus.COMPLETED),

            entity(8L, "Fullmetal Alchemist",
                "Hiromu Arakawa", "Acción",
                Format.MANGA, PublicationStatus.COMPLETED),

            entity(9L, "Cien años de soledad",
                "Gabriel García Márquez", "Realismo mágico",
                Format.BOOK, PublicationStatus.COMPLETED),

            entity(10L, "Death Note",
                "Tsugumi Ohba", "Thriller",
                Format.MANGA, PublicationStatus.COMPLETED),

            entity(11L, "One Piece",
                "Eiichiro Oda", "Aventura",
                Format.MANGA, PublicationStatus.ONGOING)
        );
    }

    private BookEntity entity(
            Long id,
            String title,
            String author,
            String genre,
            Format format,
            PublicationStatus status) {

        BookEntity entity = mock(BookEntity.class);

        when(entity.toBook()).thenReturn(
            new Book(id, title, author, genre, format, status)
        );

        return entity;
    }

    @Test
    void sinFiltros_devuelveTodoElCatalogo() {
        assertThat(catalog.list(null, null)).hasSize(11);
    }

    @Test
    void losIdsSonUnicos() {
        List<Book> books = catalog.list(null, null);

        assertThat(new HashSet<>(
            books.stream().map(Book::id).toList()
        )).hasSize(books.size());
    }

    @Test
    void filtraPorFormato() {
        assertThat(catalog.list(Format.MANGA, null))
            .isNotEmpty()
            .allMatch(book -> book.format() == Format.MANGA);
    }

    @Test
    void combinaFormatoYEstatus() {
        assertThat(catalog.list(
                Format.MANGA, PublicationStatus.ONGOING))
            .extracting(Book::title)
            .containsExactly("Berserk", "One Piece");
    }

    @MockitoSettings(strictness = Strictness.LENIENT)
    @Test
    void findById_existenteYNo() {
        BookEntity watchmen = sampleBooks().get(2);

        when(repository.findById(3L))
            .thenReturn(Optional.of(watchmen));

        when(repository.findById(999L))
            .thenReturn(Optional.empty());

        assertThat(catalog.findById(3))
            .map(Book::title)
            .contains("Watchmen");

        assertThat(catalog.findById(999)).isEmpty();
    }

    @Test
    void buscarPorTitulo_ignoraMayusculasYEspacios() {
        assertThat(catalog.list(null, null, "  DUNE  "))
            .extracting(Book::title)
            .containsExactly("Dune");
    }

    @Test
    void buscarPorAutor() {
        assertThat(catalog.list(null, null, "Herbert"))
            .extracting(Book::title)
            .containsExactly("Dune");
    }

    @Test
    void buscarPorGenero_ignoraAcentos() {
        assertThat(catalog.list(null, null, "superheroes"))
            .extracting(Book::title)
            .containsExactly("Watchmen");
    }

    @Test
    void buscarCombinandoFiltros() {
        assertThat(catalog.list(
                Format.MANGA,
                PublicationStatus.ONGOING,
                "One"))
            .extracting(Book::title)
            .containsExactly("One Piece");
    }

    @Test
    void buscarSinCoincidencias_devuelveListaVacia() {
        assertThat(catalog.list(
                null, null, "zzzinexistente"))
            .isEmpty();
    }
}
