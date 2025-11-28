package com.market.live;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class FeatureExtractor {

    public FeatureVector extract(List<Double> prices) {
        int size = prices.size();
        double last = prices.get(size - 1);
        double prev = prices.get(size - 2);

        double _return = (last - prev) / prev;

        double sma5 = avg(prices, size - 5, size);
        double sma10 = avg(prices, size - 10, size);

        double ema5 = ema(prices, 5);
        double volatility = computeVolatility(prices);

        return FeatureVector.builder()
                .sma_5(sma5)
                .sma_10(sma10)
                .ema_5(ema5)
                .volatility(volatility)
                ._return(_return)
                .build();
    }

    private double avg(List<Double> list, int start, int end) {
        return list.subList(start, end).stream()
                .mapToDouble(d -> d).average().orElse(0.0);
    }

    private double ema(List<Double> prices, int period) {
        double k = 2.0 / (period + 1);
        double ema = prices.get(0);
        for (double p : prices) ema = p * k + ema * (1 - k);
        return ema;
    }

    private double computeVolatility(List<Double> prices) {
        double mean = prices.stream().mapToDouble(d -> d).average().orElse(0.0);
        double variance = prices.stream()
                .mapToDouble(p -> Math.pow(p - mean, 2)).average().orElse(0.0);
        return Math.sqrt(variance);
    }
}
