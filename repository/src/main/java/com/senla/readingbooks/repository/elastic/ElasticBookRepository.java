package com.senla.readingbooks.repository.elastic;

import com.senla.readingbooks.document.BookDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticBookRepository extends ElasticsearchRepository<BookDocument, Long> {

    @Query("""
            {
              "bool": {
                "should": [
                  {
                    "multi_match": {
                      "query": "?0",
                      "fields": ["title^3", "annotation"],
                      "type": "best_fields",
                      "operator": "or"
                    }
                  },
                  {
                    "match_phrase": {
                      "title": {
                        "query": "?0",
                        "boost": 3
                      }
                    }
                  },
                  {
                    "match_phrase": {
                      "annotation": {
                        "query": "?0"
                      }
                    }
                  }
                ],
                "minimum_should_match": 1
              }
            }
            """)
    Page<BookDocument> searchByTitleOrAnnotation(String query, Pageable pageable);

}
