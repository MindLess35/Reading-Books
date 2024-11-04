package com.senla.readingbooks.entity.collection;

import com.senla.readingbooks.entity.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "book_collections")
public class BookCollection implements Serializable {
    @Serial
    private static final long serialVersionUID = -7084707888389572128L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 32)
    private String title;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isDraft = true;

    @ToString.Exclude
    @OneToMany(mappedBy = "bookCollection", cascade = CascadeType.ALL)
    private List<BookCollectionItem> bookCollectionItems;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, optional = false, mappedBy = "bookCollection")
    private BookCollectionStats bookCollectionStats;
}