
package mx.losmergeados.recomendador.book.source;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

class OpenLibraryClientTest {

    private HttpServer server;
    private OpenLibraryClient client;
    private final AtomicReference<String> responseBody = new AtomicReference<>();
    private final java.util.concurrent.atomic.AtomicInteger responseStatus =
        new java.util.concurrent.atomic.AtomicInteger(200);

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);

        responseBody.set("""
        {
          "docs": [
            {
              "key": "/works/OL123W",
              "title": "The Hobbit",
              "author_name": ["J. R. R. Tolkien"],
              "subject": ["Fantasy", "Adventure"],
              "first_publish_year": 1937,
              "cover_i": 12345
            }
          ]
        }
        """);

        server.createContext("/search.json", exchange -> {
            byte[] body = responseBody.get().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set(
                    "Content-Type", "application/json; charset=UTF-8");
            int status = responseStatus.get();
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        server.start();

        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        client = new OpenLibraryClient(baseUrl);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void searchByTitleTransformsResponse() {
        List<OpenLibraryBook> books = client.searchByTitle("The Hobbit");

        assertEquals(1, books.size());

        OpenLibraryBook book = books.get(0);
        assertEquals("/works/OL123W", book.sourceId());
        assertEquals("The Hobbit", book.title());
        assertEquals(List.of("J. R. R. Tolkien"), book.authors());
        assertEquals(List.of("Fantasy", "Adventure"), book.subjects());
        assertEquals(1937, book.firstPublishYear());
        assertEquals(
                "https://covers.openlibrary.org/b/id/12345-M.jpg",
                book.coverUrl());
        assertEquals(
                 "https://openlibrary.org/works/OL123W",
                book.sourceUrl());
    }

    @Test
    void searchByTitleReturnsEmptyListForBlankTitle() {
        assertTrue(client.searchByTitle(" ").isEmpty());
        assertTrue(client.searchByTitle(null).isEmpty());
    }


    @Test
    void searchByTitleHandlesMissingOptionalFields() {
        responseBody.set("""
            {
              "docs": [
                {
                  "key": "/works/OL456W",
                  "title": "A Book Without Metadata"
                }
              ]
            }
            """);

        List<OpenLibraryBook> books =
                client.searchByTitle("A Book Without Metadata");

        assertEquals(1, books.size());

        OpenLibraryBook book = books.get(0);

        assertEquals("/works/OL456W", book.sourceId());
        assertEquals("A Book Without Metadata", book.title());
        assertTrue(book.authors().isEmpty());
        assertTrue(book.subjects().isEmpty());
        assertEquals(null, book.firstPublishYear());
        assertEquals(null, book.coverUrl());
        assertEquals(
                "https://openlibrary.org/works/OL456W",
                book.sourceUrl());
    }

    @Test
    void searchByTitleReturnsEmptyListWhenNoResultsFound() {
        responseBody.set("""
                {
                "numFound": 0,
                "docs": []
                }
                """);

        List<OpenLibraryBook> books = client.searchByTitle("Nonexistent Book XYZ");

        assertTrue(books.isEmpty());
    }


    @Test
    void searchByTitleThrowsExceptionWhenServerReturns503() {
        responseStatus.set(503);
        responseBody.set("Servicio temporalmente no disponible");

        OpenLibraryException exception = assertThrows(
                OpenLibraryException.class,
                () -> client.searchByTitle("The Hobbit")
        );

        assertEquals(
                "No se pudo consultar Open Library.",
                exception.getMessage()
        );
    }


    @Test
    void searchByTitleThrowsExceptionWhenConnectionFails() throws IOException {
        server.stop(0);

        OpenLibraryException exception = assertThrows(
                OpenLibraryException.class,
                () -> client.searchByTitle("The Hobbit")
        );

        assertEquals(
                "No se pudo consultar Open Library.",
                exception.getMessage()
        );
    }



}
