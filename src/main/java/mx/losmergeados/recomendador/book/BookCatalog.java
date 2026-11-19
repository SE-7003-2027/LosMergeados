
package mx.losmergeados.recomendador.book;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class BookCatalog {

    private final BookRepository repository;

    public BookCatalog(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> list(
            Format format,
            PublicationStatus status) {

        return list(format, status, null);
    }

    public List<Book> list(
            Format format,
            PublicationStatus status,
            String query) {

        String search = normalize(
            query == null ? "" : query.trim()
        );

        return repository.findAll()
            .stream()
            .map(BookEntity::toBook)
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

    private static String normalize(String value) {
        return Normalizer
            .normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT);
    }

    public Optional<Book> findById(long id) {
        return repository.findById(id)
            .map(BookEntity::toBook);
    }
}
