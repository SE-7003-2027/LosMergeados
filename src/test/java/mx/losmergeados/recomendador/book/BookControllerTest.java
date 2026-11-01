
package mx.losmergeados.recomendador.book;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import mx.losmergeados.recomendador.book.source.OpenLibraryBook;
import mx.losmergeados.recomendador.book.source.OpenLibraryException;

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
    void obtenerObraExistente_incluyeLosCamposDelDetalle()
            throws Exception {

        mockMvc.perform(get("/api/books/9"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.title")
                        .value("Cien años de soledad"))
                .andExpect(jsonPath("$.author")
                        .value("Gabriel García Márquez"))
                .andExpect(jsonPath("$.genre")
                        .value("Realismo mágico"))
                .andExpect(jsonPath("$.format").value("BOOK"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
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
    void vistaWeb_incluyeElDialogoDeDetalle()
            throws Exception {

        mockMvc.perform(get("/index.html"))
                .andExpect(content().string(
                        containsString("<dialog id=\"detail\"")));
    }

    @Test
    void vistaWeb_scriptConsultaElDetalleEnElBackend()
            throws Exception {

        mockMvc.perform(get("/js/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("fetch(`${API_URL}/${encodeURIComponent(id)}`")));
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
    void buscarPorGeneroDesdeAPI() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("q", "superheroes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Watchmen"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void buscarSoloEspacios_devuelveTodoElCatalogo()
            throws Exception {

        mockMvc.perform(get("/api/books")
                        .param("q", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(11));
    }

    @Test
    void cadaResultadoDeBusquedaAbreSuDetalle() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("q", "Akira"))
                .andExpect(jsonPath("$[0].id").value(2));

        mockMvc.perform(get("/api/books/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Akira"));
    }

    @Test
    void buscarSinResultadosDesdeAPI() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("q", "zzzinexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


        /* ---------- GET /api/books/external ---------- */

        @Test
        void buscarEnOpenLibrary_debeRetornarResultados() throws Exception {
        OpenLibraryBook book = new OpenLibraryBook(
                "/works/OL123W",
                "The Hobbit",
                List.of("J. R. R. Tolkien"),
                List.of("Fantasy", "Adventure"),
                1937,
                "https://covers.openlibrary.org/b/id/123-M.jpg",
                "https://openlibrary.org/works/OL123W"
        );

        when(openLibraryClient.searchByTitle("The Hobbit"))
                .thenReturn(List.of(book));

        mockMvc.perform(get("/api/books/external")
                        .param("title", "The Hobbit"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].sourceId")
                        .value("/works/OL123W"))
                .andExpect(jsonPath("$[0].title")
                        .value("The Hobbit"))
                .andExpect(jsonPath("$[0].authors[0]")
                        .value("J. R. R. Tolkien"))
                .andExpect(jsonPath("$[0].firstPublishYear")
                        .value(1937));

        verify(openLibraryClient).searchByTitle("The Hobbit");
        }

        @Test
        void buscarEnOpenLibrary_sinResultados_debeRetornarListaVacia()
                throws Exception {

        when(openLibraryClient.searchByTitle("Libro inexistente"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/books/external")
                        .param("title", "Libro inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void buscarEnOpenLibrary_sinTitulo_debeRetornar400()
                throws Exception {

        mockMvc.perform(get("/api/books/external"))
                .andExpect(status().isBadRequest());
        }

        @Test
        void buscarEnOpenLibrary_siFallaElServicio_devuelve502()
                throws Exception {

        when(openLibraryClient.searchByTitle("The Hobbit"))
                .thenThrow(new OpenLibraryException(
                        "No se pudo consultar Open Library.",
                        new RuntimeException("Error de conexión")
                ));

        mockMvc.perform(get("/api/books/external")
                        .param("title", "The Hobbit"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title")
                        .value("Error en el servicio externo"))
                .andExpect(jsonPath("$.detail")
                        .value("No fue posible consultar Open Library."));
        }

    @MockBean
    private mx.losmergeados.recomendador.book.source.OpenLibraryClient openLibraryClient;
}
