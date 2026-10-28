package com.project.server.services.generation;

import com.project.server.dtos.generation.EngineGenerationRequest;
import com.project.server.dtos.generation.EngineGenerationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Talks to the Python generation engine (engine/) over plain HTTP. Internal-only by design — see
 * Almanac Blueprint §09 — this client is the one thing on the Java side allowed to reach it.
 *
 * Explicitly uses SimpleClientHttpRequestFactory (HttpURLConnection-based) rather than Spring's
 * default auto-detected JDK HttpClient factory — the JDK client's upgrade negotiation isn't
 * understood by uvicorn's h11 HTTP parser and gets the request rejected outright before FastAPI
 * ever sees a body.
 */
@Service
@Slf4j
public class GenerationEngineClient {
    private final RestClient restClient;

    public GenerationEngineClient(@Value("${project.generation-engine.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }

    public EngineGenerationResponse generate(EngineGenerationRequest request) {
        log.info("Calling generation engine with {} sessions", request.getSessions().size());

        return restClient.post()
                .uri("/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(EngineGenerationResponse.class);
    }
}
