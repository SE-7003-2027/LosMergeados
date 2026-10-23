
package mx.losmergeados.recomendador.book;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de la capa web del catálogo de obras.
 *
 * Usa @WebMvcTest para cargar solo el contexto web
 * junto con el catálogo en memoria.
 */
@WebMvcTest(BookController.class)
@Import(BookCatalog.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /* ---------- GET /api/books ---------- */

    @Test
    void listAll_debeResponderOk() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk());
    }

    @Test
    void listAll_debeRetornarJson() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON));
    }

    @Test
    void listAll_debeRetornarAlMenosUnElemento() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(jsonPath("$.length()")
                        .value(greaterThan(0)));
    }

    @Test
    void listAll_primerElementoTieneLosCamposDelModelo()
            throws Exception {

        mockMvc.perform(get("/api/books"))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].title").isString())
                .andExpect(jsonPath("$[0].author").isString())
                .andExpect(jsonPath("$[0].genre").isString())
                .andExpect(jsonPath("$[0].format").isString())
                .andExpect(jsonPath("$[0].status").isString());
    }

    @Test
    void listAll_todosTienenEstatusDePublicacionValido()
            throws Exception {

        mockMvc.perform(get("/api/books"))
                .andExpect(jsonPath("$[*].status").value(
                        everyItem(in(List.of(
                                "ONGOING",
                                "COMPLETED",
                                "HIATUS",
                                "CANCELLED"
                        )))));
    }

    @Test
    void filtrarPorFormato() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("format", "COMIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title")
                        .value(is(List.of("Watchmen", "Saga"))));
    }

    @Test
    void filtrarPorEstatus() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("status", "HIATUS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title")
                        .value(is(List.of("Saga"))));
    }

    @Test
    void filtroSinResultados_devuelveListaVacia()
            throws Exception {

        mockMvc.perform(get("/api/books")
                        .param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void formatoInvalido_devuelve400() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("format", "REVISTA"))
                .andExpect(status().isBadRequest());
    }

    /* ---------- GET /api/books/{id} ---------- */

    @Test
    void obtenerObraExistente() throws Exception {
        mockMvc.perform(get("/api/books/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Watchmen"))
                .andExpect(jsonPath("$.format").value("COMIC"));
    }

    @Test
    void obtenerObraInexistente_devuelve404()
            throws Exception {

        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Obra no encontrada"));
    }

    @Test
    void idNoNumerico_devuelve400() throws Exception {
        mockMvc.perform(get("/api/books/abc"))
                .andExpect(status().isBadRequest());
    }

    /* ---------- Vista web ---------- */

    @Test
    void vistaWeb_debeServirseComoHtml()
            throws Exception {

        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.TEXT_HTML));
    }

    @Test
    void vistaWeb_laRaizRedirigeAlIndex()
            throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(forwardedUrl("index.html"));
    }

    /* ---------- Nuevas pruebas: búsqueda REST ---------- */

    @Test
    void buscarPorTituloDesdeAPI() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("q", "Dune"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Dune"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void buscarPorAutorDesdeAPI() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("q", "Herbert"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Dune"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void buscarIgnorandoAcentos() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("q", "anos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title")
                        .value("Cien años de soledad"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void buscarConFiltrosCombinados() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("q", "One")
                        .param("format", "MANGA")
                        .param("status", "ONGOING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title")
                        .value("One Piece"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void buscarSinResultadosDesdeAPI() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("q", "zzzinexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
