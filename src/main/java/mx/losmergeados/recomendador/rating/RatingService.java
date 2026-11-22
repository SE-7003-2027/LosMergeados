package mx.losmergeados.recomendador.rating;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.losmergeados.recomendador.book.BookEntity;
import mx.losmergeados.recomendador.book.BookRepository;
import mx.losmergeados.recomendador.user.UserEntity;
import mx.losmergeados.recomendador.user.UserRepository;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public RatingService(
            RatingRepository ratingRepository,
            UserRepository userRepository,
            BookRepository bookRepository) {

        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public Rating create(
            Long userId,
            Long bookId,
            Integer score) {

        if (score == null || score < 1 || score > 10) {
            throw new IllegalArgumentException(
                    "La puntuación debe estar entre 1 y 10."
            );
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el usuario indicado."
                ));

        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el libro indicado."
                ));

        if (ratingRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new IllegalArgumentException(
                    "El usuario ya ha valorado este libro."
            );
        }

        RatingEntity rating = new RatingEntity(
                user,
                book,
                score
        );

        RatingEntity saved = ratingRepository.save(rating);

        return new Rating(
                saved.getId(),
                saved.getUser().getId(),
                saved.getBook().getId(),
                saved.getScore()
        );
    }

    @Transactional(readOnly = true)
    public java.util.List<Rating> findByBookId(Long bookId) {

        if (!bookRepository.existsById(bookId)) {
            throw new IllegalArgumentException(
                    "No existe el libro indicado."
            );
        }

        return ratingRepository.findByBookId(bookId)
                .stream()
                .map(rating -> new Rating(
                        rating.getId(),
                        rating.getUser().getId(),
                        rating.getBook().getId(),
                        rating.getScore()
                ))
                .toList();
    }
}