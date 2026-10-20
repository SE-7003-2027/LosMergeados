package mx.losmergeados.recomendador.book;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * API REST del catálogo de obras.
 *
 * <p>El controlador no guarda datos: delega en {@link BookCatalog}. Así, al
 * conectar la base de datos solo cambia el servicio.
 */
@RestController
@RequestMapping("/api/books")
@Tag(name = "Catálogo", description = "Consulta de libros, cómics y manga")
public class BookController {

    private final BookCatalog catalog;

    public BookController(BookCatalog catalog) {
        this.catalog = catalog;
    }

    /**
     * Lista las obras, con filtros opcionales por formato y estatus.
     * Un valor inválido (por ejemplo {@code ?format=REVISTA}) responde {@code 400}.
     */
    @GetMapping
    @Operation(summary = "Lista el catálogo de obras",
            description = "Sin parámetros devuelve todo el catálogo. Los filtros se pueden combinar.")
    public List<Book> list(
            @Parameter(description = "Formato: BOOK, COMIC o MANGA")
            @RequestParam(required = false) Format format,
            @Parameter(description = "Estatus: ONGOING, COMPLETED, HIATUS o CANCELLED")
            @RequestParam(required = false) PublicationStatus status) {
        return catalog.list(format, status);
    }

    /**
     * Devuelve el detalle de una obra, o {@code 404} si no existe.
     * Un id no numérico responde {@code 400}.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una obra")
    public Book get(@PathVariable long id) {
        return catalog.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Obra no encontrada"));
    }
}
