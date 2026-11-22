
package mx.losmergeados.recomendador.book;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.losmergeados.recomendador.book.recommendation.RecommendationService;
import mx.losmergeados.recomendador.book.source.OpenLibraryBook;
import mx.losmergeados.recomendador.book.source.OpenLibraryClient;

/**
 * API REST del catálogo de obras.
 *
 * El controlador no guarda datos: delega en BookCatalog.
 * Permite listar, buscar, filtrar y consultar detalles.
 */
@RestController
@RequestMapping("/api/books")
@Tag(name = "Catálogo", description = "Consulta de libros, cómics y manga")
public class BookController {

    private final BookCatalog catalog;
    private final OpenLibraryClient openLibraryClient;
    private final RecommendationService recommendationService;

    public BookController(
            BookCatalog catalog,
            OpenLibraryClient openLibraryClient,
            RecommendationService recommendationService)  {
        this.catalog = catalog;
        this.openLibraryClient = openLibraryClient;
        this.recommendationService = recommendationService;
    }

    /**
     * Lista y busca obras del catálogo.
     *
     * Permite combinar:
     * - Búsqueda textual por título, autor o género.
     * - Filtro por formato.
     * - Filtro por estatus de publicación.
     *
     * Los parámetros son opcionales.
     * Los formatos o estatus inválidos responden 400.
     */
    @GetMapping
    @Operation(
        summary = "Lista y busca obras del catálogo",
        description = "Permite buscar por título, autor o género y combinar filtros de formato y estatus."
    )
    public List<Book> list(

            @Parameter(description = "Formato: BOOK, COMIC o MANGA")
            @RequestParam(required = false)
            Format format,

            @Parameter(description = "Estatus: ONGOING, COMPLETED, HIATUS o CANCELLED")
            @RequestParam(required = false)
            PublicationStatus status,

            @Parameter(description = "Texto de búsqueda por título, autor o género")
            @RequestParam(name = "q", required = false)
            String query) {

        return catalog.list(format, status, query);
    }


    /**
     * Busca obras en el catálogo externo de Open Library.
     *
     * No modifica ni mezcla los resultados con el catálogo interno.
     */
    @GetMapping("/external")
    @Operation(
        summary = "Busca obras en Open Library",
        description = "Consulta obras externas por título sin modificar el catálogo interno."
    )
    public List<OpenLibraryBook> searchExternal(
            @Parameter(description = "Título de la obra que se desea buscar")
            @RequestParam String title) {

        return openLibraryClient.searchByTitle(title);
    }

    /**
     * Consulta el detalle de una obra por ID.
     *
     * Respuestas:
     * 200 - Obra encontrada.
     * 404 - Obra inexistente.
     * 400 - Identificador no numérico.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una obra")
    public Book get(@PathVariable long id) {

        return catalog.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Obra no encontrada"
                        )
                );
    }

    @GetMapping("/{id}/recommendations")
    @Operation(
        summary = "Obtiene recomendaciones para una obra",
        description = "Devuelve hasta cinco obras similares según autor, género, formato y estatus."
    )
    public List<Book> recommendations(@PathVariable long id) {

        try {
            return recommendationService.recommend(id);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Obra no encontrada"
            );
        }
    }

    /**
     * Importa una obra seleccionada de Open Library al catálogo local.
     */
    @PostMapping("/import")
    @Operation(
        summary = "Importa una obra de Open Library",
        description = "Guarda una obra externa en el catálogo local, asignándole formato y estatus."
    )
    public Book importBook(@RequestBody ImportBookRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La solicitud de importación es obligatoria."
            );
        }

        if (request.format() == null || request.status() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El formato y el estatus son obligatorios."
            );
        }

        return catalog.importFromOpenLibrary(
            request.book(),
            request.format(),
            request.status()
        );
    }

}
