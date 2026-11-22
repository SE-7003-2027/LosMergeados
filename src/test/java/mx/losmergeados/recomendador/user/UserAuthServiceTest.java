
package mx.losmergeados.recomendador.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserAuthService userAuthService;

    @BeforeEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_debeAutenticarYCrearSesion() {

        UserLoginRequest request =
                new UserLoginRequest("jimmy_test", "password-de-prueba");

        UserEntity userEntity = new UserEntity(
                "jimmy_test",
                "jimmy_test@example.com",
                "$2a$10$hashDePrueba"
        );

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenReturn(authentication);

        when(authentication.getName()).thenReturn("jimmy_test");

        when(userRepository.findByUsername("jimmy_test"))
                .thenReturn(Optional.of(userEntity));

        User result = userAuthService.login(
                request,
                httpRequest,
                httpResponse
        );

        assertThat(result.username()).isEqualTo("jimmy_test");
        assertThat(result.email()).isEqualTo("jimmy_test@example.com");

        verify(securityContextRepository).saveContext(
                any(SecurityContext.class),
                eq(httpRequest),
                eq(httpResponse)
        );
    }

    @Test
    void login_conCredencialesIncorrectas_debeLanzarExcepcion() {

        UserLoginRequest request =
                new UserLoginRequest("jimmy_test", "password-incorrecta");

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenThrow(new BadCredentialsException("Credenciales inválidas"));

        assertThatThrownBy(() -> userAuthService.login(
                request,
                httpRequest,
                httpResponse
        )).isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByUsername(anyString());

        verify(securityContextRepository, never()).saveContext(
                any(SecurityContext.class),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        );
    }

    @Test
    void login_usuarioInexistente_debeLanzarExcepcion() {

        UserLoginRequest request =
                new UserLoginRequest("usuario_inexistente", "password-prueba");

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenReturn(authentication);

        when(authentication.getName()).thenReturn("usuario_inexistente");

        when(userRepository.findByUsername("usuario_inexistente"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAuthService.login(
                request,
                httpRequest,
                httpResponse
        )).isInstanceOf(java.util.NoSuchElementException.class);

        verify(securityContextRepository, never()).saveContext(
                any(SecurityContext.class),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        );
    }
}
