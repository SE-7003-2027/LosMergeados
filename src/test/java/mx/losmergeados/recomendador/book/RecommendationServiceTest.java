package mx.losmergeados.recomendador.book;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import mx.losmergeados.recomendador.book.recommendation.RecommendationService;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private BookRepository repository;

    private RecommendationService service;

    @BeforeEach
    void setUp() {
        service = new RecommendationService(repository);
    }

    @Test
    void recommend_mismoAutor_debePriorizarLaObra() {
        BookEntity target = book(
                1L,
                "Obra objetivo",
                "Autor A",
                "Fantasy",
                Format.BOOK,
                PublicationStatus.COMPLETED
        );

        BookEntity sameAuthor = book(
                2L,
                "Mismo autor",
                "Autor A",
                "Otro genero",
                Format.COMIC,
                PublicationStatus.ONGOING
        );

        BookEntity sameGenre = book(
                3L,
                "Mismo genero",
                "Autor B",
                "Fantasy",
                Format.BOOK,
                PublicationStatus.COMPLETED
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(target));

        when(repository.findAll())
                .thenReturn(List.of(target, sameAuthor, sameGenre));

        List<Book> recommendations = service.recommend(1L);

        assertEquals(2, recommendations.size());
        assertEquals(2L, recommendations.get(0).id());
        assertEquals(3L, recommendations.get(1).id());
    }

    @Test
    void recommend_sinClasificar_noDebeAportarCoincidenciaDeGenero() {
        BookEntity target = book(
                1L,
                "Obra objetivo",
                "Autor A",
                "Sin clasificar",
                Format.BOOK,
                PublicationStatus.COMPLETED
        );

        BookEntity sameGenre = book(
                2L,
                "Otra obra",
                "Autor B",
                "Sin clasificar",
                Format.BOOK,
                PublicationStatus.COMPLETED
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(target));

        when(repository.findAll())
                .thenReturn(List.of(target, sameGenre));

        List<Book> recommendations = service.recommend(1L);

        assertEquals(1, recommendations.size());

        assertEquals(20, scoreOf(
                target,
                sameGenre
        ));
    }

    @Test
    void recommend_noDebeIncluirLaObraOriginal() {
        BookEntity target = book(
                1L,
                "Obra objetivo",
                "Autor A",
                "Fantasy",
                Format.BOOK,
                PublicationStatus.COMPLETED
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(target));

        when(repository.findAll())
                .thenReturn(List.of(target));

        List<Book> recommendations = service.recommend(1L);

        assertTrue(recommendations.isEmpty());
        assertFalse(
                recommendations.stream()
                        .anyMatch(book -> book.id().equals(1L))
        );
    }

    @Test
    void recommend_debeOrdenarPorPuntuacionDescendente() {
        BookEntity target = book(
                1L,
                "Obra objetivo",
                "Autor A",
                "Fantasy",
                Format.BOOK,
                PublicationStatus.COMPLETED
        );

        BookEntity lowScore = book(
                2L,
                "Baja",
                "Autor B",
                "Other",
                Format.BOOK,
                PublicationStatus.ONGOING
        );

        BookEntity highScore = book(
                3L,
                "Alta",
                "Autor A",
                "Fantasy",
                Format.BOOK,
                PublicationStatus.COMPLETED
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(target));

        when(repository.findAll())
                .thenReturn(List.of(
                        target,
                        lowScore,
                        highScore
                ));

        List<Book> recommendations = service.recommend(1L);

        assertEquals(2, recommendations.size());
        assertEquals(3L, recommendations.get(0).id());
        assertEquals(2L, recommendations.get(1).id());
    }

    private static BookEntity book(
            Long id,
            String title,
            String author,
            String genre,
            Format format,
            PublicationStatus status) {

        return new BookEntity(
                id,
                title,
                author,
                genre,
                format,
                status
        );
    }

    private static int scoreOf(
            BookEntity target,
            BookEntity candidate) {

        int score = 0;

        if (target.getAuthor().equals(candidate.getAuthor())) {
            score += 50;
        }

        if (!target.getGenre().equals("Sin clasificar")
                && target.getGenre().equals(candidate.getGenre())) {
            score += 30;
        }

        if (target.getFormat() == candidate.getFormat()) {
            score += 15;
        }

        if (target.getStatus() == candidate.getStatus()) {
            score += 5;
        }

        return score;
    }
}