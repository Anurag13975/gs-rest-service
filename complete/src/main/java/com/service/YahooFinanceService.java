package com.service;

import com.model.MarketPrice;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class YahooFinanceService {

    private final WebClient client = WebClient.builder()
            .baseUrl("https://query1.finance.yahoo.com")
            .defaultHeader("User-Agent", "Mozilla/5.0")
            .build();

    public Mono<MarketPrice> fetchLivePrice(String symbol) {

        String url = "/v8/finance/chart/" + symbol + "?interval=1m&range=1d";

        return client.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> YahooParser.parsePrice(response, symbol));
    }
}
