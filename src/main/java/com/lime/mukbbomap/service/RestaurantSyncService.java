package com.lime.mukbbomap.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.lime.mukbbomap.document.RestaurantDocument;
import com.lime.mukbbomap.domain.Restaurant;
import com.lime.mukbbomap.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantSyncService {
    private final RestaurantRepository restaurantRepository;
    private final ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "restaurants";

    @Transactional(readOnly = true)
    public void syncAllRestaurants() {
        log.info("Starting restaurant sync to Elasticsearch");

        try {
            List<Restaurant> restaurants = restaurantRepository.findAll();

            List<BulkOperation> bulkOperations = restaurants.stream()
                    .map(restaurant -> {
                        RestaurantDocument doc = RestaurantDocument.from(restaurant);
                        return BulkOperation.of(b -> b
                                .index(i -> i
                                        .index(INDEX_NAME)
                                        .id(doc.getId())
                                        .document(doc)
                                )
                        );
                    })
                    .collect(Collectors.toList());

            if (!bulkOperations.isEmpty()) {
                BulkRequest bulkRequest = BulkRequest.of(b -> b
                        .operations(bulkOperations)
                );

                BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);

                if (bulkResponse.errors()) {
                    log.error("Bulk indexing had errors");
                } else {
                    log.info("Synced {} restaurants to Elasticsearch", restaurants.size());
                }
            }

        } catch (IOException e) {
            log.error("Failed to sync restaurants", e);
            throw new RuntimeException("Sync failed", e);
        }
    }

    public void syncRestaurant(Restaurant restaurant) {
        try {
            RestaurantDocument doc = RestaurantDocument.from(restaurant);

            IndexRequest<RestaurantDocument> request = IndexRequest.of(i -> i
                    .index(INDEX_NAME)
                    .id(doc.getId())
                    .document(doc)
            );

            elasticsearchClient.index(request);
            log.info("Synced restaurant: {}", restaurant.getId());

        } catch (IOException e) {
            log.error("Failed to sync restaurant: {}", restaurant.getId(), e);
            throw new RuntimeException("Sync failed", e);
        }
    }

    public void deleteRestaurant(Long restaurantId) {
        try {
            elasticsearchClient.delete(d -> d
                    .index(INDEX_NAME)
                    .id(restaurantId.toString())
            );
            log.info("Deleted restaurant from index: {}", restaurantId);

        } catch (IOException e) {
            log.error("Failed to delete restaurant: {}", restaurantId, e);
            throw new RuntimeException("Delete failed", e);
        }
    }
}