package mx.losmergeados.recomendador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del Recomendador de libros, cómics y manga de LosMergeados.
 *
 * <p>Levanta la API REST ({@code /api/...}), la vista web ({@code /})
 * y la documentación interactiva ({@code /swagger-ui.html}) en el puerto 8080.
 */
@SpringBootApplication
public class RecomendadorApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecomendadorApplication.class, args);
    }
}
