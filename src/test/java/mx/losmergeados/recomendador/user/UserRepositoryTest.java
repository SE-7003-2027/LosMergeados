package mx.losmergeados.recomendador.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void debeGuardarYEncontrarUsuarioPorUsername() {
        UserEntity user = new UserEntity(
                "jimmy",
                "jimmy@example.com",
                "hash-de-prueba"
        );

        UserEntity saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();

        UserEntity found = userRepository
                .findByUsername("jimmy")
                .orElseThrow();

        assertThat(found.getUsername()).isEqualTo("jimmy");
        assertThat(found.getEmail()).isEqualTo("jimmy@example.com");
        assertThat(found.getPasswordHash()).isEqualTo("hash-de-prueba");
    }

    @Test
    void debeEncontrarUsuarioPorEmail() {
        UserEntity user = new UserEntity(
                "alice",
                "alice@example.com",
                "otro-hash"
        );

        userRepository.save(user);

        UserEntity found = userRepository
                .findByEmail("alice@example.com")
                .orElseThrow();

        assertThat(found.getUsername()).isEqualTo("alice");
    }

    @Test
    void noDebePermitirUsernameDuplicado() {
        userRepository.saveAndFlush(
                new UserEntity(
                        "jimmy",
                        "jimmy@example.com",
                        "hash-1"
                )
        );

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(
                        new UserEntity(
                                "jimmy",
                                "otro@example.com",
                                "hash-2"
                        )
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void noDebePermitirEmailDuplicado() {
        userRepository.saveAndFlush(
                new UserEntity(
                        "jimmy",
                        "jimmy@example.com",
                        "hash-1"
                )
        );

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(
                        new UserEntity(
                                "otro-usuario",
                                "jimmy@example.com",
                                "hash-2"
                        )
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}