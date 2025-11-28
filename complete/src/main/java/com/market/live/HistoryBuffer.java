package com.market.live;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class HistoryBuffer {

    private final int maxSize = 500;
    private final Deque<MarketPayload> buffer = new ArrayDeque<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void add(MarketPayload msg) {
        lock.writeLock().lock();
        try {
            buffer.addLast(msg);
            if (buffer.size() > maxSize) buffer.removeFirst();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<MarketPayload> latest(int limit) {
        lock.readLock().lock();
        try {
            List<MarketPayload> list = new ArrayList<>(buffer);
            int from = Math.max(0, list.size() - limit);
            return list.subList(from, list.size());
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<MarketPayload> all() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(buffer);
        } finally {
            lock.readLock().unlock();
        }
    }
}
