package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.e2e.config.E2eProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class DuplicateCheckDownloadExcelClient {

    private final WebClient webClient;

    public DuplicateCheckDownloadExcelClient(E2eProperties props) {
        this.webClient = WebClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    public DuplicateCheckDownloadExcelResult download(DuplicateCheckDownloadExcelHelper which) {

        return webClient.get()
                .uri(which.path())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ApiError.class)
                                .map(DuplicateCheckDownloadExcelResult.Failure::new)
                                .flatMap(failure -> Mono.error(new DownloadFailedException(failure)))
                )
                // Header + Body lesen
                .toEntity(byte[].class)
                .map(this::toSuccess)
                .map(DuplicateCheckDownloadExcelResult.class::cast)
                .onErrorResume(DownloadFailedException.class, ex -> Mono.just(ex.result()))
                .block();
    }

    private DuplicateCheckDownloadExcelResult.Success toSuccess(ResponseEntity<byte[]> entity) {
        byte[] body = entity.getBody() == null ? new byte[0] : entity.getBody();
        HttpHeaders headers = entity.getHeaders() == null ? new HttpHeaders() : entity.getHeaders();
        return new DuplicateCheckDownloadExcelResult.Success(body, headers);
    }

    private static class DownloadFailedException extends RuntimeException {
        private final DuplicateCheckDownloadExcelResult.Failure result;

        DownloadFailedException(DuplicateCheckDownloadExcelResult.Failure result) {
            this.result = result;
        }

        DuplicateCheckDownloadExcelResult.Failure result() {
            return result;
        }
    }
}
