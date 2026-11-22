
package mx.losmergeados.recomendador.user;

/**
 * Representa la información pública de un usuario.
 *
 * No incluye la contraseña ni su hash.
 *
 * @param id identificador único
 * @param username nombre de usuario
 * @param email correo electrónico
 */
public record User(
        Long id,
        String username,
        String email
) {}
