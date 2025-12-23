package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.e2e.config.E2eProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class PageVisitsClient {

    private final WebClient webClient;

    public PageVisitsClient(E2eProperties props) {
        this.webClient = WebClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    public PageVisitsResult pageVisited(String pageId) {

        return webClient.put()
                .uri("/api/maintenance/page-visited/{pageId}", pageId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ApiError.class)
                                .map(PageVisitsResult.Failure::new)
                                .flatMap(failure -> Mono.error(new PageVisitsFailedException(failure)))
                )
                .toBodilessEntity()
                .thenReturn((PageVisitsResult) new PageVisitsResult.Success())
                .onErrorResume(PageVisitsFailedException.class, ex -> Mono.just(ex.result()))
                .block();
    }

    private static class PageVisitsFailedException extends RuntimeException {
        private final PageVisitsResult.Failure result;

        PageVisitsFailedException(PageVisitsResult.Failure result) {
            this.result = result;
        }

        PageVisitsResult.Failure result() {
            return result;
        }
    }
}
