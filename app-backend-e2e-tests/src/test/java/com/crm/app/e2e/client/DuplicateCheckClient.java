package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.e2e.config.E2eProperties;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

public class DuplicateCheckClient {

    private final WebClient webClient;

    public DuplicateCheckClient(E2eProperties props) {
        this.webClient = WebClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    public CrmUploadResult duplicateCheck(
            String emailAddress,
            String loginToken,
            String sourceSystem,
            Resource file
    ) {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("emailAddress", emailAddress);
        body.add("sourceSystem", sourceSystem);
        body.add("file", file);

        return webClient.post()
                .uri("/api/duplicate-check")
                .headers(headers ->
                        headers.setBearerAuth(loginToken)
                )
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ApiError.class)
                                .map(CrmUploadResult.Failure::new)
                                .flatMap(f ->
                                        reactor.core.publisher.Mono.error(
                                                new DuplicateCheckFailedException(f)
                                        )
                                )
                )
                .toBodilessEntity()
                .map(r -> new CrmUploadResult.Success())
                .map(CrmUploadResult.class::cast)
                .onErrorResume(DuplicateCheckFailedException.class,
                        ex -> reactor.core.publisher.Mono.just(ex.result())
                )
                .block();
    }

    private static class DuplicateCheckFailedException extends RuntimeException {
        private final CrmUploadResult.Failure result;

        DuplicateCheckFailedException(CrmUploadResult.Failure result) {
            this.result = result;
        }

        CrmUploadResult.Failure result() {
            return result;
        }
    }
}
