package com.senla.readingbooks.entity.collection;

import com.senla.readingbooks.entity.AuditingEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "book_collection_stats")
public class BookCollectionStats extends AuditingEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    private BookCollection bookCollection;

    @Builder.Default
    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;

    @Builder.Default
    @Column(name = "dislikes_count", nullable = false)
    private Integer dislikesCount = 0;

    @Column(name = "rating")
    private Float rating;

    @Builder.Default
    @Column(name = "ratings_count", nullable = false)
    private Integer ratingsCount = 0;

    @Builder.Default
    @Column(name = "views_count", nullable = false)
    private Integer viewsCount = 0;

}
