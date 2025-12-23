package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.dto.CrmUploadHistoryResponse;
import com.crm.app.e2e.config.E2eProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;

public class CrmUploadHistoryClient {

    private final WebClient webClient;

    public CrmUploadHistoryClient(E2eProperties props) {
        this.webClient = WebClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    public CrmUploadHistoryResult getCrmUploadHistory(String emailAddress, String loginToken) {

        return webClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                // wichtig: gleicher Pfad wie Controller
                                .path("/api/customer/get-upload-history/{emailAddress}")
                                .build(emailAddress)
                )
                .headers(headers -> headers.setBearerAuth(loginToken))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ApiError.class)
                                .map(CrmUploadHistoryResult.Failure::new)
                                .flatMap(f ->
                                        reactor.core.publisher.Mono.error(
                                                new CrmUploadHistoryFailedException(f)
                                        )
                                )
                )
                .bodyToMono(CrmUploadHistoryResponse.class)
                .map(CrmUploadHistoryResult.Success::new)
                .map(CrmUploadHistoryResult.class::cast)
                .onErrorResume(CrmUploadHistoryFailedException.class,
                        ex -> reactor.core.publisher.Mono.just(ex.result())
                )
                .block();
    }

    private static class CrmUploadHistoryFailedException extends RuntimeException {
        private final CrmUploadHistoryResult.Failure result;

        CrmUploadHistoryFailedException(CrmUploadHistoryResult.Failure result) {
            this.result = result;
        }

        CrmUploadHistoryResult result() {
            return result;
        }
    }
}
