package com.senla.readingbooks.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Getter
@Setter
@AllArgsConstructor
@Setting(settingPath = "/elasticsearch/elastic-settings.json")
@Document(indexName = "book_collections")
public class BookCollectionDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "russian_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "russian_analyzer")
    private String description;

}
