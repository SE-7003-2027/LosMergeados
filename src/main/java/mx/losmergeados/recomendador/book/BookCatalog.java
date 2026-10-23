
package mx.losmergeados.recomendador.book;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static mx.losmergeados.recomendador.book.Format.*;
import static mx.losmergeados.recomendador.book.PublicationStatus.*;

/**
 * Servicio del catálogo de libros, cómics y manga.
 *
 * Utiliza datos de ejemplo en memoria.
 * Permite listar, filtrar, buscar y consultar por ID.
 */
@Service
public class BookCatalog {

    private final List<Book> books = List.of(
        new Book(1L, "El nombre del viento",
            "Patrick Rothfuss", "Fantasía", BOOK, ONGOING),

        new Book(2L, "Akira",
            "Katsuhiro Otomo", "Ciencia ficción", MANGA, COMPLETED),

        new Book(3L, "Watchmen",
            "Alan Moore", "Superhéroes", COMIC, COMPLETED),

        new Book(4L, "Dune",
            "Frank Herbert", "Ciencia ficción", BOOK, COMPLETED),

        new Book(5L, "Berserk",
            "Kentaro Miura", "Fantasía oscura", MANGA, ONGOING),

        new Book(6L, "Saga",
            "Brian K. Vaughan", "Ciencia ficción", COMIC, HIATUS),

        new Book(7L, "La sombra del viento",
            "Carlos Ruiz Zafón", "Misterio", BOOK, COMPLETED),

        new Book(8L, "Fullmetal Alchemist",
            "Hiromu Arakawa", "Acción", MANGA, COMPLETED),

        new Book(9L, "Cien años de soledad",
            "Gabriel García Márquez", "Realismo mágico", BOOK, COMPLETED),

        new Book(10L, "Death Note",
            "Tsugumi Ohba", "Thriller", MANGA, COMPLETED),

        new Book(11L, "One Piece",
            "Eiichiro Oda", "Aventura", MANGA, ONGOING)
    );

    /**
     * Método original para mantener compatibilidad.
     */
    public List<Book> list(
            Format format,
            PublicationStatus status) {

        return list(format, status, null);
    }

    /**
     * Busca obras por título, autor o género.
     * También admite filtros combinados.
     */
    public List<Book> list(
            Format format,
            PublicationStatus status,
            String query) {

        String search = normalize(
            query == null ? "" : query.trim()
        );

        return books.stream()
            .filter(book ->
                format == null || book.format() == format)
            .filter(book ->
                status == null || book.status() == status)
            .filter(book ->
                search.isEmpty() ||
                normalize(
                    book.title() + " " +
                    book.author() + " " +
                    book.genre()
                ).contains(search)
            )
            .toList();
    }

    /**
     * Normaliza texto para ignorar mayúsculas y acentos.
     */
    private static String normalize(String value) {
        return Normalizer
            .normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT);
    }

    /**
     * Consulta una obra mediante su identificador.
     */
    public Optional<Book> findById(long id) {
        return books.stream()
            .filter(book -> book.id() == id)
            .findFirst();
    }
}
