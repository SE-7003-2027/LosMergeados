package mx.losmergeados.recomendador.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Estado del servicio. Útil para comprobar rápidamente que la aplicación
 * está arriba (y, más adelante, para CI o despliegue).
 */
@RestController
@Tag(name = "Sistema")
public class HealthController {

    @GetMapping("/api/health")
    @Operation(summary = "Estado del servicio")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
