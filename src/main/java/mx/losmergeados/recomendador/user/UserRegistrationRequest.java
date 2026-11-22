
package mx.losmergeados.recomendador.user;

/**
 * Representa los datos necesarios para registrar un usuario.
 *
 * @param username nombre de usuario
 * @param email correo electrónico
 * @param password contraseña en texto plano recibida
 */
public record UserRegistrationRequest(
        String username,
        String email,
        String password
) {}
