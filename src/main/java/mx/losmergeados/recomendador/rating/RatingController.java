package mx.losmergeados.recomendador.rating;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import mx.losmergeados.recomendador.user.UserService;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;
    private final UserService userService;

    public RatingController(
            RatingService ratingService,
            UserService userService) {
        this.ratingService = ratingService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Rating> create(
            @RequestBody RatingRequest request,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión para valorar una obra."
            );
        }

        if (request == null || request.bookId() == null
                || request.score() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El libro y la puntuación son obligatorios."
            );
        }

        try {
            Long userId = userService
                    .findByUsername(authentication.getName())
                    .id();

            Rating rating = ratingService.create(
                    userId,
                    request.bookId(),
                    request.score()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(rating);

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );
        }
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Rating>> getByBookId(
            @PathVariable Long bookId) {

        try {
            return ResponseEntity.ok(
                    ratingService.findByBookId(bookId)
            );

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    exception.getMessage()
            );
        }
    }
}