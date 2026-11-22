
package mx.losmergeados.recomendador.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import mx.losmergeados.recomendador.config.SecurityConfig;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private UserAuthService userAuthService;

    @Test
    void registrarUsuario_debeRetornar201() throws Exception {

        User user = new User(
                1L,
                "jimmy",
                "jimmy@example.com"
        );

        when(userService.register(any(UserRegistrationRequest.class)))
                .thenReturn(user);

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "jimmy",
                            "email": "jimmy@example.com",
                            "password": "mi-password"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("jimmy"))
                .andExpect(jsonPath("$.email").value("jimmy@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registrarUsuario_usernameDuplicado_debeRetornar400()
            throws Exception {

        when(userService.register(any(UserRegistrationRequest.class)))
                .thenThrow(new IllegalArgumentException(
                        "El nombre de usuario ya está registrado."
                ));

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "jimmy",
                            "email": "jimmy@example.com",
                            "password": "mi-password"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarUsuario_emailDuplicado_debeRetornar400()
            throws Exception {

        when(userService.register(any(UserRegistrationRequest.class)))
                .thenThrow(new IllegalArgumentException(
                        "El correo electrónico ya está registrado."
                ));

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "jimmy",
                            "email": "jimmy@example.com",
                            "password": "mi-password"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerUsuarioAutenticado_debeRetornar200() throws Exception {

        User user = new User(
                1L,
                "jimmy",
                "jimmy@example.com"
        );

        when(userService.findByUsername("jimmy"))
                .thenReturn(user);

        mockMvc.perform(get("/api/users/me")
                .with(SecurityMockMvcRequestPostProcessors.authentication(
                        new UsernamePasswordAuthenticationToken(
                                "jimmy",
                                null,
                                AuthorityUtils.createAuthorityList("ROLE_USER")
                        )
                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("jimmy"))
                .andExpect(jsonPath("$.email").value("jimmy@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void obtenerUsuarioNoAutenticado_debeRetornar401() throws Exception {

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
