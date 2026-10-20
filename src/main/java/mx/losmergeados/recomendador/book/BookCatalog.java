package mx.losmergeados.recomendador.book;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static mx.losmergeados.recomendador.book.Format.BOOK;
import static mx.losmergeados.recomendador.book.Format.COMIC;
import static mx.losmergeados.recomendador.book.Format.MANGA;
import static mx.losmergeados.recomendador.book.PublicationStatus.COMPLETED;
import static mx.losmergeados.recomendador.book.PublicationStatus.HIATUS;
import static mx.losmergeados.recomendador.book.PublicationStatus.ONGOING;

/**
 * Catálogo de obras (capa de servicio).
 *
 * <p>Por ahora usa datos de ejemplo en memoria. Cuando se conecte la base de
 * datos, este es el único lugar que hay que reemplazar (por un repositorio
 * JPA); el controlador solo depende de estos métodos, así que no cambia.
 */
@Service
public class BookCatalog {

    private final List<Book> books = List.of(
            new Book(1L,  "El nombre del viento",     "Patrick Rothfuss",       "Fantasía",         BOOK,  ONGOING),
            new Book(2L,  "Akira",                    "Katsuhiro Otomo",        "Ciencia ficción",  MANGA, COMPLETED),
            new Book(3L,  "Watchmen",                 "Alan Moore",             "Superhéroes",      COMIC, COMPLETED),
            new Book(4L,  "Dune",                     "Frank Herbert",          "Ciencia ficción",  BOOK,  COMPLETED),
            new Book(5L,  "Berserk",                  "Kentaro Miura",          "Fantasía oscura",  MANGA, ONGOING),
            new Book(6L,  "Saga",                     "Brian K. Vaughan",       "Ciencia ficción",  COMIC, HIATUS),
            new Book(7L,  "La sombra del viento",     "Carlos Ruiz Zafón",      "Misterio",         BOOK,  COMPLETED),
            new Book(8L,  "Fullmetal Alchemist",      "Hiromu Arakawa",         "Acción",           MANGA, COMPLETED),
            new Book(9L,  "Cien años de soledad",     "Gabriel García Márquez", "Realismo mágico",  BOOK,  COMPLETED),
            new Book(10L, "Death Note",               "Tsugumi Ohba",           "Thriller",         MANGA, COMPLETED),
            new Book(11L, "One Piece",                "Eiichiro Oda",           "Aventura",         MANGA, ONGOING)
    );

    /**
     * Lista las obras del catálogo, con filtros opcionales.
     *
     * @param format formato a filtrar, o {@code null} para no filtrar
     * @param status estatus a filtrar, o {@code null} para no filtrar
     * @return obras que cumplen ambos filtros, en el orden del catálogo
     */
    public List<Book> list(Format format, PublicationStatus status) {
        return books.stream()
                .filter(book -> format == null || book.format() == format)
                .filter(book -> status == null || book.status() == status)
                .toList();
    }

    /**
     * Busca una obra por su identificador.
     *
     * @param id identificador de la obra
     * @return la obra, o vacío si no existe
     */
    public Optional<Book> findById(long id) {
        return books.stream()
                .filter(book -> book.id() == id)
                .findFirst();
    }
}
