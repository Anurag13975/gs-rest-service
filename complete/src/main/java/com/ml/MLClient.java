package com.ml;

import com.market.live.FeatureVector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class MLClient {

    private final WebClient client;

    public MLClient() {
        this.client = WebClient.builder()
                .baseUrl("https://ml-service-m9ew.onrender.com")
//                .baseUrl("http://localhost:8001")
                .build();
    }

    public Mono<MLPrediction> predict(FeatureVector fv) {
        return client.post()
                .uri("/predict")
                .bodyValue(fv)
                .retrieve()
                .bodyToMono(MLPrediction.class);
    }
}
