
package mx.losmergeados.recomendador.rating;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import mx.losmergeados.recomendador.config.SecurityConfig;
import mx.losmergeados.recomendador.user.CustomUserDetailsService;
import mx.losmergeados.recomendador.user.User;
import mx.losmergeados.recomendador.user.UserService;

@WebMvcTest(RatingController.class)
@Import(SecurityConfig.class)
class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RatingService ratingService;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void crearValoracion_debeRetornar201() throws Exception {

        User user = new User(
                1L,
                "jimmy",
                "jimmy@example.com"
        );

        Rating rating = new Rating(1L, 1L, 2L, 9);

        when(userService.findByUsername("jimmy"))
                .thenReturn(user);

        when(ratingService.create(1L, 2L, 9))
                .thenReturn(rating);

        mockMvc.perform(post("/api/ratings")
                .with(SecurityMockMvcRequestPostProcessors.authentication(
                        new UsernamePasswordAuthenticationToken(
                                "jimmy",
                                null,
                                AuthorityUtils.createAuthorityList("ROLE_USER")
                        )
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content("""
                        {
                            "bookId": 2,
                            "score": 9
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.bookId").value(2))
                .andExpect(jsonPath("$.score").value(9));
    }

    @Test
    void crearValoracion_usuarioNoAutenticado_debeRetornar401()
            throws Exception {

        mockMvc.perform(post("/api/ratings")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "bookId": 2,
                            "score": 9
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearValoracion_puntuacionInvalida_debeRetornar400()
            throws Exception {

        User user = new User(
                1L,
                "jimmy",
                "jimmy@example.com"
        );

        when(userService.findByUsername("jimmy"))
                .thenReturn(user);

        when(ratingService.create(1L, 2L, 11))
                .thenThrow(new IllegalArgumentException(
                        "La puntuación debe estar entre 1 y 10."
                ));

        mockMvc.perform(post("/api/ratings")
                .with(SecurityMockMvcRequestPostProcessors.authentication(
                        new UsernamePasswordAuthenticationToken(
                                "jimmy",
                                null,
                                AuthorityUtils.createAuthorityList("ROLE_USER")
                        )
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content("""
                        {
                            "bookId": 2,
                            "score": 11
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearValoracion_libroInexistente_debeRetornar400()
            throws Exception {

        User user = new User(
                1L,
                "jimmy",
                "jimmy@example.com"
        );

        when(userService.findByUsername("jimmy"))
                .thenReturn(user);

        when(ratingService.create(1L, 999L, 9))
                .thenThrow(new IllegalArgumentException(
                        "No existe el libro indicado."
                ));

        mockMvc.perform(post("/api/ratings")
                .with(SecurityMockMvcRequestPostProcessors.authentication(
                        new UsernamePasswordAuthenticationToken(
                                "jimmy",
                                null,
                                AuthorityUtils.createAuthorityList("ROLE_USER")
                        )
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content("""
                        {
                            "bookId": 999,
                            "score": 9
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearValoracion_duplicada_debeRetornar400()
            throws Exception {

        User user = new User(
                1L,
                "jimmy",
                "jimmy@example.com"
        );

        when(userService.findByUsername("jimmy"))
                .thenReturn(user);

        when(ratingService.create(1L, 2L, 9))
                .thenThrow(new IllegalArgumentException(
                        "El usuario ya ha valorado este libro."
                ));

        mockMvc.perform(post("/api/ratings")
                .with(SecurityMockMvcRequestPostProcessors.authentication(
                        new UsernamePasswordAuthenticationToken(
                                "jimmy",
                                null,
                                AuthorityUtils.createAuthorityList("ROLE_USER")
                        )
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content("""
                        {
                            "bookId": 2,
                            "score": 9
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarValoraciones_libroConValoraciones_debeRetornar200()
            throws Exception {

        Rating rating1 = new Rating(1L, 1L, 2L, 9);
        Rating rating2 = new Rating(2L, 2L, 2L, 8);

        when(ratingService.findByBookId(2L))
                .thenReturn(List.of(rating1, rating2));

        mockMvc.perform(get("/api/ratings/book/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].bookId").value(2))
                .andExpect(jsonPath("$[0].score").value(9))
                .andExpect(jsonPath("$[1].score").value(8));
    }

    @Test
    void consultarValoraciones_libroSinValoraciones_debeRetornarListaVacia()
            throws Exception {

        when(ratingService.findByBookId(2L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ratings/book/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void consultarValoraciones_libroInexistente_debeRetornar404()
            throws Exception {

        when(ratingService.findByBookId(999L))
                .thenThrow(new IllegalArgumentException(
                        "No existe el libro indicado."
                ));

        mockMvc.perform(get("/api/ratings/book/999"))
                .andExpect(status().isNotFound());
    }
}
