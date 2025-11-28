package com.anomaly;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class AnomalyDetector {

    private int windowSize = 50;  // last 50 ticks
    private Queue<Double> priceWindow = new LinkedList<>();
    private Queue<Double> volWindow = new LinkedList<>();
    private Queue<Double> diffWindow = new LinkedList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnomalyResult {
        public boolean isAnomaly;
        public String type;
        public double score;
    }

    public AnomalyResult detect(double price, double volatility, double sma5, double sma10) {

        double diff = Math.abs(sma5 - sma10);  // divergence

        updateWindow(priceWindow, price);
        updateWindow(volWindow, volatility);
        updateWindow(diffWindow, diff);

        double priceMean = mean(priceWindow);
        double priceStd = std(priceWindow, priceMean);

        double volMean = mean(volWindow);
        double volStd = std(volWindow, volMean);

        double diffMean = mean(diffWindow);
        double diffStd = std(diffWindow, diffMean);

        // ---- 1) Z-SCORE ANOMALY ----
        if (priceStd > 0) {
            double z = Math.abs((price - priceMean) / priceStd);
            if (z > 3) {
                return AnomalyResult.builder()
                        .isAnomaly(true)
                        .type("Z_SCORE_SPIKE")
                        .score(z)
                        .build();
            }
        }

        // ---- 2) VOLATILITY SPIKE ----
        if (volStd > 0 && volatility > volMean + 2 * volStd) {
            double score = (volatility - volMean) / volStd;
            return AnomalyResult.builder()
                    .isAnomaly(true)
                    .type("VOLATILITY_SPIKE")
                    .score(score)
                    .build();
        }

        // ---- 3) SMA DIVERGENCE ----
        if (diffStd > 0 && diff > diffMean + 2 * diffStd) {
            double score = (diff - diffMean) / diffStd;
            return AnomalyResult.builder()
                    .isAnomaly(true)
                    .type("SMA_DIVERGENCE")
                    .score(score)
                    .build();
        }

        return AnomalyResult.builder()
                .isAnomaly(false)
                .type("NORMAL")
                .score(0.0)
                .build();
    }

    private void updateWindow(Queue<Double> q, double value) {
        q.add(value);
        if (q.size() > windowSize) q.poll();
    }

    private double mean(Queue<Double> q) {
        return q.stream().mapToDouble(v -> v).average().orElse(0.0);
    }

    private double std(Queue<Double> q, double mean) {
        return Math.sqrt(
                q.stream()
                        .mapToDouble(v -> (v - mean) * (v - mean))
                        .average()
                        .orElse(0.0)
        );
    }
}
