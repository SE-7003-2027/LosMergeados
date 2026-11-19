package mx.losmergeados.recomendador.book.source;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Cliente HTTP para consultar la API de Open Library.
 */
@Component
public class OpenLibraryClient {

    private static final String BASE_URL = "https://openlibrary.org";

    private final RestClient restClient;

    public OpenLibraryClient() {
        this(BASE_URL);
    }

    OpenLibraryClient(String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Busca obras por título en Open Library.
     *
     * @param title título que se desea buscar
     * @return obras encontradas, o una lista vacía si no hay resultados
     */
    public List<OpenLibraryBook> searchByTitle(String title) {
        if (title == null || title.isBlank()) {
            return List.of();
        }

        JsonNode response;

        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search.json")
                            .queryParam("title", title.trim())
                            .queryParam("limit", 10)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new OpenLibraryException(
                    "No se pudo consultar Open Library.", e);
        }

        if (response == null || !response.has("docs")
                || !response.get("docs").isArray()) {
            return List.of();
        }

        return java.util.stream.StreamSupport.stream(
                        response.get("docs").spliterator(), false)
                .map(this::toOpenLibraryBook)
                .filter(book -> book.sourceId() != null)
                .toList();
    }

    private OpenLibraryBook toOpenLibraryBook(JsonNode document) {
        String sourceId = textOrNull(document, "key");
        String title = textOrNull(document, "title");

        List<String> authors = stringList(document.get("author_name"));
        List<String> subjects = stringList(document.get("subject"));

        Integer firstPublishYear = document.has("first_publish_year")
                && document.get("first_publish_year").canConvertToInt()
                ? document.get("first_publish_year").intValue()
                : null;

        String coverId = document.has("cover_i")
                && document.get("cover_i").canConvertToInt()
                ? document.get("cover_i").asText()
                : null;

        String coverUrl = coverId == null
                ? null
                : "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";

        String sourceUrl = sourceId == null
                ? null
                : BASE_URL + sourceId;

        return new OpenLibraryBook(
                sourceId,
                title,
                authors,
                subjects,
                firstPublishYear,
                coverUrl,
                sourceUrl
        );
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);

        return value != null && value.isTextual()
                ? value.asText()
                : null;
    }

    private List<String> stringList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }

        return java.util.stream.StreamSupport.stream(
                        node.spliterator(), false)
                .filter(JsonNode::isTextual)
                .map(JsonNode::asText)
                .toList();
    }
}
