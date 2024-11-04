package com.senla.readingbooks.entity.book;

import com.senla.readingbooks.entity.AuditingEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Setter
@Getter
@Entity
@Builder
@ToString(exclude = "book")
@NoArgsConstructor
@AllArgsConstructor
public class BookStatistics extends AuditingEntityBase {

    @Id
    @Column(name = "book_id")
    private Long id;

    @MapsId  //@PrimaryKeyJoinColumn
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Book book;

    public void setBook(Book book) {
        id = book.getId();
        this.book = book;
    }

    @Builder.Default
    @Column(nullable = false)
    private Float pagesCount = 0F;

    @Builder.Default
    @Column(nullable = false)
    private Integer likesCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer charactersCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer ratingsCount = 0;

    private Float rating;

    @Builder.Default
    @Column(nullable = false)
    private Integer viewsCount = 0;

    private Instant publicationDate;

    @Builder.Default
    @Column(nullable = false)
    private Integer libraryAddCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer readingNowCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer alreadyReadCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer willReadCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer notInterestedCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer abandonedCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer downloadsCount = 0;
}

