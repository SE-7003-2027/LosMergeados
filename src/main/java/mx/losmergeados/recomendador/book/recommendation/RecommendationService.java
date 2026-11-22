package mx.losmergeados.recomendador.book.recommendation;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import mx.losmergeados.recomendador.book.Book;
import mx.losmergeados.recomendador.book.BookEntity;
import mx.losmergeados.recomendador.book.BookRepository;

@Service
public class RecommendationService {

    private static final int AUTHOR_WEIGHT = 50;
    private static final int GENRE_WEIGHT = 30;
    private static final int FORMAT_WEIGHT = 15;
    private static final int STATUS_WEIGHT = 5;

    private final BookRepository repository;

    public RecommendationService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> recommend(long bookId) {
        BookEntity target = repository.findById(bookId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La obra no existe."
                        ));

        return repository.findAll()
                .stream()
                .filter(book -> !book.getId().equals(bookId))
                .map(book -> new ScoredBook(
                        book.toBook(),
                        score(target, book)
                ))
                .filter(scored -> scored.score() > 0)
                .sorted(
                        Comparator.comparingInt(ScoredBook::score)
                                .reversed()
                                .thenComparing(
                                        scored -> scored.book().title()
                                )
                )
                .map(ScoredBook::book)
                .limit(5)
                .toList();
    }

    private int score(BookEntity target, BookEntity candidate) {
        int score = 0;

        if (sameText(target.getAuthor(), candidate.getAuthor())) {
            score += AUTHOR_WEIGHT;
        }

        if (isMeaningfulGenre(target.getGenre())
                && sameText(target.getGenre(), candidate.getGenre())) {
            score += GENRE_WEIGHT;
        }

        if (target.getFormat() == candidate.getFormat()) {
            score += FORMAT_WEIGHT;
        }

        if (target.getStatus() == candidate.getStatus()) {
            score += STATUS_WEIGHT;
        }

        return score;
    }

    private static boolean sameText(String first, String second) {
        return normalize(first).equals(normalize(second));
    }

    private static boolean isMeaningfulGenre(String genre) {
        return genre != null
                && !normalize(genre).equals("sin clasificar");
    }

    private static String normalize(String value) {
        return Normalizer
                .normalize(
                        value == null ? "" : value,
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}+", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private record ScoredBook(Book book, int score) {
    }
}