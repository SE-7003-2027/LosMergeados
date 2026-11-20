
package mx.losmergeados.recomendador.book;

import jakarta.persistence.*;

@Entity
@Table(
    name = "books",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_books_source_id",
            columnNames = "source_id"
        )
    }
)
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String genre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Format format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PublicationStatus status;

    @Column(name = "source_id", unique = true)
    private String sourceId;

    protected BookEntity() {
    }

    public BookEntity(
            String title,
            String author,
            String genre,
            Format format,
            PublicationStatus status) {

        this(title, author, genre, format, status, null);
    }

    public BookEntity(
            String title,
            String author,
            String genre,
            Format format,
            PublicationStatus status,
            String sourceId) {

        this.title = title;
        this.author = author;
        this.genre = genre;
        this.format = format;
        this.status = status;
        this.sourceId = sourceId;
    }

    BookEntity(
            Long id,
            String title,
            String author,
            String genre,
            Format format,
            PublicationStatus status) {

        this(title, author, genre, format, status);
        this.id = id;
    }

    public Book toBook() {
        return new Book(
            id,
            title,
            author,
            genre,
            format,
            status
        );
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public Format getFormat() {
        return format;
    }

    public PublicationStatus getStatus() {
        return status;
    }

    public String getSourceId() {
        return sourceId;
    }
}
