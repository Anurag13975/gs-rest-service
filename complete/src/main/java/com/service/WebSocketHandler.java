package com.service;

import com.anomaly.AnomalyDetector;
import com.constants.StockSymbolConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.live.*;
import com.ml.MLClient;
import com.ml.MLPrediction;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Data
@Slf4j
public class WebSocketHandler implements org.springframework.web.reactive.socket.WebSocketHandler {

    private final LiveMarketStream stream;
    private final PriceBuffer priceBuffer;
    private final FeatureExtractor featureExtractor;
    private final MLClient mlClient;
    private final AnomalyDetector anomalyDetector;
    private final ObjectMapper objectMapper;
    private final HistoryBuffer historyBuffer;

    @Override
    @NonNull
    public Mono<Void> handle(WebSocketSession session) {

        String symbol = StockSymbolConstants.RELIANCE;

        Flux<WebSocketMessage> outbound =
                stream.startLiveStream(symbol)
                        .flatMap(price -> {

                            // 1️⃣ store price into buffer
                            priceBuffer.addPrice(symbol, price.getPrice());

                            // 2️⃣ if we don't have enough data → send raw data
                            if (!priceBuffer.hasEnoughData(symbol)) {
                                return Mono.just(session.textMessage(
                                        rawJson(price.getSymbol(), price.getPrice(), price.getTimestamp())
                                ));
                            }

                            // 3️⃣ Extract features
                            FeatureVector fv = featureExtractor.extract(priceBuffer.getPrices(symbol));

                            // 5️⃣ Call ML service to get prediction and convert to JSON (append prediction)
                            return mlClient.predict(fv)
                                    // fallback prediction on error
                                    .onErrorReturn(new MLPrediction("error", 0.0))
                                    .map(prediction -> {
                                        AnomalyDetector.AnomalyResult anomaly = anomalyDetector.detect(
                                                price.getPrice(),
                                                fv.volatility(),
                                                fv.sma_5(),
                                                fv.sma_10()
                                        );
                                        MarketPayload marketPayload = MarketPayload.builder()
                                                .symbol(price.getSymbol())
                                                .price(price.getPrice())
                                                .ts(price.getTimestamp())
                                                .features(fv)
                                                .mlPrediction(prediction)
                                                .anomaly(anomaly)
                                                .build();
                                        historyBuffer.add(marketPayload);
                                        try {
                                            String jsonString = objectMapper.writeValueAsString(marketPayload);
                                            return session.textMessage(jsonString);
                                        } catch (Exception e) {
                                            log.error("JSON serialization error:{}", e.getMessage());
                                            return session.textMessage("");
                                        }
                                    });
                        });

        return session.send(outbound);
    }

    private String rawJson(String symbol, double price, long timestamp) {
        MarketPayload payload = MarketPayload.builder()
                .symbol(symbol)
                .price(price)
                .ts(timestamp)
                .build();
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("JSON serialization error:{}", e.getMessage());
            return "";
        }
    }
}
