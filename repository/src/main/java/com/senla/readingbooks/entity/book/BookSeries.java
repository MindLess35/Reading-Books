package com.senla.readingbooks.entity.book;

import com.senla.readingbooks.entity.AuditingEntityBase;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.book.PublicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@ToString(exclude = "books")
@NoArgsConstructor
@AllArgsConstructor
public class BookSeries extends AuditingEntityBase implements Serializable {
    @Serial
    private static final long serialVersionUID = 6942089735481203231L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String title;

    @Column(length = 255)
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PublicationStatus status = PublicationStatus.IS_DRAFT;

    @OneToMany(mappedBy = "bookSeries", fetch = FetchType.LAZY)
    private transient List<Book> books;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
