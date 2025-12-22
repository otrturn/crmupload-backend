package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.e2e.config.E2eProperties;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

public class CrmUploadClient {

    private final WebClient webClient;

    public CrmUploadClient(E2eProperties props) {
        this.webClient = WebClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    public CrmUploadResult upload(
            String emailAddress,
            String loginToken,
            String sourceSystem,
            String crmSystem,
            String crmUrl,
            String crmCustomerId,
            String crmApiKey,
            Resource file
    ) {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("emailAddress", emailAddress);
        body.add("sourceSystem", sourceSystem);
        body.add("crmSystem", crmSystem);
        body.add("crmUrl", crmUrl);
        body.add("crmCustomerId", crmCustomerId);
        body.add("crmApiKey", crmApiKey);
        body.add("file", file);

        return webClient.post()
                .uri("/api/crm-upload")
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
                                                new UploadFailedException(f)
                                        )
                                )
                )
                .toBodilessEntity()
                .map(r -> new CrmUploadResult.Success())
                .map(CrmUploadResult.class::cast)
                .onErrorResume(UploadFailedException.class,
                        ex -> reactor.core.publisher.Mono.just(ex.result())
                )
                .block();
    }

    private static class UploadFailedException extends RuntimeException {
        private final CrmUploadResult.Failure result;

        UploadFailedException(CrmUploadResult.Failure result) {
            this.result = result;
        }

        CrmUploadResult.Failure result() {
            return result;
        }
    }
}
