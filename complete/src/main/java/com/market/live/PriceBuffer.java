package com.market.live;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PriceBuffer {

    private final Map<String, Deque<Double>> buffers = new ConcurrentHashMap<>();
    private final int windowSize = 20; // rolling window of 20 points
    private final int minDataPoints = 10; // minimum data points required

    public void addPrice(String symbol, double price) {
        buffers.putIfAbsent(symbol, new ArrayDeque<>());
        Deque<Double> q = buffers.get(symbol);

        if (q.size() >= windowSize) {
            q.pollFirst(); // remove oldest
        }
        q.addLast(price);
    }

    public List<Double> getPrices(String symbol) {
        return new ArrayList<>(buffers.getOrDefault(symbol, new ArrayDeque<>()));
    }

    public boolean hasEnoughData(String symbol) {
        return buffers.containsKey(symbol) && buffers.get(symbol).size() >= minDataPoints;
    }
}
