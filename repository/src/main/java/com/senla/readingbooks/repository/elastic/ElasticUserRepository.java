package com.senla.readingbooks.repository.elastic;

import com.senla.readingbooks.document.UserDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticUserRepository extends ElasticsearchRepository<UserDocument, Long> {

    @Query("""
            {
              "bool": {
                "should": [
                  {
                    "match": {
                      "username": {
                        "query": "?0"
                      }
                    }
                  }
                ]
              }
            }
            """)
    Page<UserDocument> searchByUsername(String query, Pageable pageable);
}
