package mx.losmergeados.recomendador;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que la aplicación completa arranca.
 *
 * <p>Si estas pruebas fallan, hay un error de configuración que impide
 * que la aplicación levante.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RecomendadorApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Si Spring no puede levantar el contexto, esta prueba falla automáticamente.
    }

    @Test
    void documentacionOpenApi_estaDisponible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}
