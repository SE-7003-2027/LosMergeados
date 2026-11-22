
package mx.losmergeados.recomendador.rating;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import mx.losmergeados.recomendador.book.BookEntity;
import mx.losmergeados.recomendador.book.BookRepository;
import mx.losmergeados.recomendador.book.Format;
import mx.losmergeados.recomendador.book.PublicationStatus;
import mx.losmergeados.recomendador.user.UserEntity;
import mx.losmergeados.recomendador.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private RatingService ratingService;

    @Mock
    private UserEntity user;

    @Mock
    private BookEntity book;

    @BeforeEach
    void setUp() {
    }

    @Test
    void crearValoracion_debeGuardarValoracion() {

        when(user.getId()).thenReturn(1L);

        when(book.getId()).thenReturn(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(ratingRepository.existsByUserIdAndBookId(1L, 1L))
                .thenReturn(false);

        when(ratingRepository.save(any(RatingEntity.class)))
                .thenAnswer(invocation -> {
                    RatingEntity rating = invocation.getArgument(0);

                    return new RatingEntity(
                            rating.getUser(),
                            rating.getBook(),
                            rating.getScore()
                    ) {
                        @Override
                        public Long getId() {
                            return 1L;
                        }
                    };
                });

        Rating result = ratingService.create(1L, 1L, 9);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.bookId()).isEqualTo(1L);
        assertThat(result.score()).isEqualTo(9);

        verify(ratingRepository).save(any(RatingEntity.class));
    }

    @Test
    void crearValoracion_puntuacionMenorAlMinimo_debeLanzarExcepcion() {

        assertThatThrownBy(() -> ratingService.create(1L, 1L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La puntuación debe estar entre 1 y 10.");

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void crearValoracion_puntuacionMayorAlMaximo_debeLanzarExcepcion() {

        assertThatThrownBy(() -> ratingService.create(1L, 1L, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La puntuación debe estar entre 1 y 10.");

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void crearValoracion_usuarioInexistente_debeLanzarExcepcion() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.create(1L, 1L, 9))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No existe el usuario indicado.");

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void crearValoracion_libroInexistente_debeLanzarExcepcion() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.create(1L, 1L, 9))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No existe el libro indicado.");

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void crearValoracion_duplicada_debeLanzarExcepcion() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(ratingRepository.existsByUserIdAndBookId(1L, 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> ratingService.create(1L, 1L, 9))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El usuario ya ha valorado este libro.");

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void consultarValoraciones_libroExistente_debeRetornarLista() {

        when(bookRepository.existsById(1L)).thenReturn(true);

        RatingEntity rating = org.mockito.Mockito.mock(RatingEntity.class);

        when(rating.getId()).thenReturn(1L);
        when(rating.getUser()).thenReturn(user);
        when(rating.getBook()).thenReturn(book);
        when(rating.getScore()).thenReturn(9);

        when(user.getId()).thenReturn(1L);
        when(book.getId()).thenReturn(1L);

        when(ratingRepository.findByBookId(1L))
                .thenReturn(java.util.List.of(rating));

        var result = ratingService.findByBookId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).score()).isEqualTo(9);
        assertThat(result.get(0).userId()).isEqualTo(1L);
        assertThat(result.get(0).bookId()).isEqualTo(1L);
    }

    @Test
    void consultarValoraciones_libroSinValoraciones_debeRetornarListaVacia() {

        when(bookRepository.existsById(1L)).thenReturn(true);
        when(ratingRepository.findByBookId(1L))
                .thenReturn(java.util.List.of());

        var result = ratingService.findByBookId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void consultarValoraciones_libroInexistente_debeLanzarExcepcion() {

        when(bookRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> ratingService.findByBookId(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No existe el libro indicado.");
    }

}
