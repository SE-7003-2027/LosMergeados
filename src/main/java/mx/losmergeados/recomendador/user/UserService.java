
package mx.losmergeados.recomendador.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(UserRegistrationRequest request) {

        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException(
                    "El nombre de usuario ya está registrado."
            );
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException(
                    "El correo electrónico ya está registrado."
            );
        }

        String passwordHash = passwordEncoder.encode(request.password());

        UserEntity userEntity = new UserEntity(
                request.username(),
                request.email(),
                passwordHash
        );

        UserEntity savedUser = userRepository.save(userEntity);

        return new User(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }

    public User findByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un usuario con ese nombre."
                ));

        return new User(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
