package com.lime.mukbbomap.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.lime.mukbbomap.document.RestaurantDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantSearchService {
    private final ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "restaurants";

    public List<RestaurantDocument> searchRestaurants(String keyword, int page, int size) {
        try {
            Query nameQuery = Query.of(q -> q
                    .match(m -> m
                            .field("name")
                            .query(keyword)
                            .boost(2.0f)
                    )
            );

            Query descriptionQuery = Query.of(q -> q
                    .match(m -> m
                            .field("description")
                            .query(keyword)
                    )
            );

            Query boolQuery = Query.of(q -> q
                    .bool(BoolQuery.of(b -> b
                            .should(nameQuery)
                            .should(descriptionQuery)
                    ))
            );

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .query(boolQuery)
                    .from(page * size)
                    .size(size)
            );

            SearchResponse<RestaurantDocument> response =
                    elasticsearchClient.search(searchRequest, RestaurantDocument.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Elasticsearch search error", e);
            throw new RuntimeException("Search failed", e);
        }
    }

    public List<RestaurantDocument> searchByCategory(String category, int page, int size) {
        try {
            Query categoryQuery = Query.of(q -> q
                    .term(t -> t
                            .field("category")
                            .value(category)
                    )
            );

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .query(categoryQuery)
                    .from(page * size)
                    .size(size)
            );

            SearchResponse<RestaurantDocument> response =
                    elasticsearchClient.search(searchRequest, RestaurantDocument.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Elasticsearch search error", e);
            throw new RuntimeException("Search failed", e);
        }
    }
}