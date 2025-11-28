package com.service;

import com.model.MarketPrice;
import lombok.Data;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Component
@Data
public class LiveMarketStream {

    private final YahooFinanceService yahooService;

    public Flux<MarketPrice> startLiveStream(String symbol) {
        return Flux.interval(Duration.ofSeconds(5))
                .flatMap(tick -> yahooService.fetchLivePrice(symbol))
                .share(); // allow multiple subscribers
    }
}
