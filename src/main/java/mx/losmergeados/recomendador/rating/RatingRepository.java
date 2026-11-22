
package mx.losmergeados.recomendador.rating;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository
        extends JpaRepository<RatingEntity, Long> {

    List<RatingEntity> findByBookId(Long bookId);

    List<RatingEntity> findByUserId(Long userId);

    Optional<RatingEntity> findByUserIdAndBookId(
            Long userId,
            Long bookId
    );

    boolean existsByUserIdAndBookId(
            Long userId,
            Long bookId
    );
}
