
package mx.losmergeados.recomendador.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationRequest request;

    @BeforeEach
    void setUp() {
        request = new UserRegistrationRequest(
                "jimmy",
                "jimmy@example.com",
                "mi-password"
        );
    }

    @Test
    void debeRegistrarUsuarioCorrectamente() {
        when(userRepository.findByUsername("jimmy"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("jimmy@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("mi-password"))
                .thenReturn("hash-de-prueba");

        UserEntity savedEntity = new UserEntity(
                "jimmy",
                "jimmy@example.com",
                "hash-de-prueba"
        );

        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(savedEntity);

        User result = userService.register(request);

        assertThat(result.username()).isEqualTo("jimmy");
        assertThat(result.email()).isEqualTo("jimmy@example.com");
        assertThat(result.id()).isNull();

        verify(passwordEncoder).encode("mi-password");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void debeRechazarUsernameDuplicado() {
        when(userRepository.findByUsername("jimmy"))
                .thenReturn(Optional.of(
                        new UserEntity(
                                "jimmy",
                                "jimmy@example.com",
                                "hash"
                        )
                ));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre de usuario ya está registrado.");

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void debeRechazarEmailDuplicado() {
        when(userRepository.findByUsername("jimmy"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("jimmy@example.com"))
                .thenReturn(Optional.of(
                        new UserEntity(
                                "otro-usuario",
                                "jimmy@example.com",
                                "hash"
                        )
                ));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El correo electrónico ya está registrado.");

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void debeGuardarLaContraseñaComoHash() {
        when(userRepository.findByUsername("jimmy"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("jimmy@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("mi-password"))
                .thenReturn("$2a$12$hash-ficticio");

        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity entity = invocation.getArgument(0);

                    assertThat(entity.getPasswordHash())
                            .isEqualTo("$2a$12$hash-ficticio");

                    assertThat(entity.getPasswordHash())
                            .isNotEqualTo("mi-password");

                    return entity;
                });

        userService.register(request);

        verify(passwordEncoder).encode("mi-password");
    }
}
