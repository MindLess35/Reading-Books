package com.senla.readingbooks.entity.book;

import com.senla.readingbooks.entity.AuditingEntityBase;
import com.senla.readingbooks.entity.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "book_reviews")
@ToString(exclude = {"book", "user"})
public class BookReview extends AuditingEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "rating")
    private Float rating;

    @Builder.Default
    @Column(name = "views_count", nullable = false)
    private Integer viewsCount = 0;

    @Builder.Default
    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;

    @Builder.Default
    @Column(name = "dislikes_count", nullable = false)
    private Integer dislikesCount = 0;

    @Column(name = "is_spoiler", nullable = false)
    private Boolean isSpoiler;

    @Column(nullable = false)
    private String contentUrl;

}

