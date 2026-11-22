
package mx.losmergeados.recomendador.user;

/**
 * Representa las credenciales necesarias para iniciar sesión.
 *
 * @param username nombre de usuario
 * @param password contraseña en texto plano recibida
 */
public record UserLoginRequest(
        String username,
        String password
) {}
