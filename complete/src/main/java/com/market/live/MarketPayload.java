package com.market.live;

import com.anomaly.AnomalyDetector;
import com.ml.MLPrediction;
import lombok.Builder;

@Builder
public record MarketPayload(
        String symbol,
        double price,
        long ts,
        FeatureVector features,
        MLPrediction mlPrediction,
        AnomalyDetector.AnomalyResult anomaly
) {}
