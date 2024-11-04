package com.senla.readingbooks.entity.book;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.senla.readingbooks.entity.collection.BookCollectionItem;
import com.senla.readingbooks.entity.user.PurchasedBook;
import com.senla.readingbooks.entity.user.ReadingProgress;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.entity.user.UserLibrary;
import com.senla.readingbooks.enums.book.AccessType;
import com.senla.readingbooks.enums.book.BookForm;
import com.senla.readingbooks.enums.book.Genre;
import com.senla.readingbooks.enums.book.PublicationStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "books")
@ToString(onlyExplicitlyIncluded = true)
public class Book implements Serializable {
    @Serial
    private static final long serialVersionUID = -3154566435256965952L;

    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Include
    @Column(nullable = false, length = 32)
    private String title;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BookForm form;

    @Builder.Default
    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PublicationStatus status = PublicationStatus.IS_DRAFT;

    @ToString.Include
    @Builder.Default
    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @ToString.Include
    @Column(nullable = false, length = 255)
    private String annotation;

    @ToString.Include
    @Column(length = 255)
    private String authorNote;

    @ToString.Include
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AccessType accessType = AccessType.FREE;

    @ToString.Include
    private String coverUrl;

    @JsonIgnore
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @Column(name = "genre")
    @CollectionTable(name = "book_genres", joinColumns = @JoinColumn(name = "book_id"))
    private Set<Genre> genres;

    @JsonIgnore
    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "tag")
    @CollectionTable(name = "book_tags", joinColumns = @JoinColumn(name = "book_id"))
    private Set<String> tags;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private List<User> users;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "series_id")
    private BookSeries bookSeries;

    @JsonIgnore
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private transient List<Chapter> chapters;

    @JsonIgnore
    @OneToOne(mappedBy = "book", optional = false, fetch = FetchType.LAZY)
    private BookStatistics bookStatistics;

    @JsonIgnore
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private transient List<UserLibrary> userLibraries;

    @JsonIgnore
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private transient List<PurchasedBook> purchasedBooks;

    @JsonIgnore
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private transient List<ReadingProgress> readingProgress;

    @JsonIgnore
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private transient List<BookCollectionItem> bookCollectionItems;

    @JsonIgnore
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private transient List<BookVisit> bookVisits;

}
