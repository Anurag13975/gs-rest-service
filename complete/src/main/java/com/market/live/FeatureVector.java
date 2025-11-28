package com.market.live;

import lombok.Builder;

@Builder
public record FeatureVector(
        double _return,
        double sma_5,
        double sma_10,
        double ema_5,
        double volatility
) {}
