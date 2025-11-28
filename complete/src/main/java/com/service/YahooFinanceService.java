package com.service;

import com.model.MarketPrice;
import com.model.YahooMarketPriceResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class YahooFinanceService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://query1.finance.yahoo.com")
            .build();

    public Mono<MarketPrice> fetchLivePrice(String symbol) {
        String url = "/v8/finance/chart/" + symbol + "?interval=1m&range=1d";

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(YahooMarketPriceResponse.class)
                .map(response -> {
                    Double price = response.getChart()
                            .getResult()
                            .get(0)
                            .getMeta()
                            .getRegularMarketPrice();
                    long timestamp = System.currentTimeMillis();
                    return MarketPrice.builder()
                            .symbol(symbol)
                            .price(price)
                            .timestamp(timestamp)
                            .build();
                });
    }
}
