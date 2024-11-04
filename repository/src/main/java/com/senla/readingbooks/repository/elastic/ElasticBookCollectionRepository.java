package com.senla.readingbooks.repository.elastic;

import com.senla.readingbooks.document.BookCollectionDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticBookCollectionRepository extends ElasticsearchRepository<BookCollectionDocument, Long> {

    @Query("""
            {
              "bool": {
                "should": [
                  {
                    "multi_match": {
                      "query": "?0",
                      "fields": ["title^3", "description"],
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
                      "description": {
                        "query": "?0"
                      }
                    }
                  }
                ],
                "minimum_should_match": 1
              }
            }
            """)
    Page<BookCollectionDocument> searchByTitleOrDescription(String query, Pageable pageable);
}
