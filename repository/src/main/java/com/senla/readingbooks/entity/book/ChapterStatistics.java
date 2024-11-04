package com.senla.readingbooks.entity.book;


import com.senla.readingbooks.entity.AuditingEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
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
@ToString(exclude = {"chapter"})
@NoArgsConstructor
@AllArgsConstructor
public class ChapterStatistics extends AuditingEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Chapter chapter;

    @Builder.Default
    @Column(nullable = false)
    private Integer viewsCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Float pagesCount = 0F;

    @Builder.Default
    @Column(nullable = false)
    private Integer charactersCount = 0;

}
