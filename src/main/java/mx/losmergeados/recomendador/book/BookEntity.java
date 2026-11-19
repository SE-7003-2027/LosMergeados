
package mx.losmergeados.recomendador.book;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
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

    protected BookEntity() {
    }

    public BookEntity(
            String title,
            String author,
            String genre,
            Format format,
            PublicationStatus status) {

        this.title = title;
        this.author = author;
        this.genre = genre;
        this.format = format;
        this.status = status;
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

    // Getters

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

        BookEntity(
            Long id,
            String title,
            String author,
            String genre,
            Format format,
            PublicationStatus status) {

        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.format = format;
        this.status = status;
    }
}

