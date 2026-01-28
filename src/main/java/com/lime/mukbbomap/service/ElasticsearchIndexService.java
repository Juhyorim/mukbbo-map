package com.lime.mukbbomap.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchIndexService {
    private final ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "restaurants";

    @PostConstruct
    public void initIndex() {
        try {
            if (!indexExists()) {
                createIndex();
                log.info("Created index: {}", INDEX_NAME);
            } else {
                log.info("Index already exists: {}", INDEX_NAME);
            }
        } catch (IOException e) {
            log.error("Failed to initialize index", e);
        }
    }

    private boolean indexExists() throws IOException {
        BooleanResponse response = elasticsearchClient.indices()
                .exists(ExistsRequest.of(e -> e.index(INDEX_NAME)));
        return response.value();
    }

    private void createIndex() throws IOException {
        String indexDefinition = """
            {
              "settings": {
                "analysis": {
                  "analyzer": {
                    "nori": {
                      "type": "nori"
                    }
                  }
                }
              },
              "mappings": {
                "properties": {
                  "name": {
                    "type": "text",
                    "analyzer": "nori"
                  },
                  "category": {
                    "type": "keyword"
                  },
                  "description": {
                    "type": "text",
                    "analyzer": "nori"
                  },
                  "address": {
                    "type": "text"
                  },
                  "latitude": {
                    "type": "double"
                  },
                  "longitude": {
                    "type": "double"
                  },
                  "geohash": {
                    "type": "keyword"
                  },
                  "phoneNumber": {
                    "type": "keyword"
                  },
                  "rating": {
                    "type": "double"
                  }
                }
              }
            }
            """;

        elasticsearchClient.indices().create(CreateIndexRequest.of(c -> c
                .index(INDEX_NAME)
                .withJson(new StringReader(indexDefinition))
        ));
    }
}