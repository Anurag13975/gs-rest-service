package com.controller;

import com.market.live.HistoryBuffer;
import com.market.live.MarketPayload;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HistoryController {

    private final HistoryBuffer historyBuffer;

    public HistoryController(HistoryBuffer historyBuffer) {
        this.historyBuffer = historyBuffer;
    }

    @GetMapping("/history")
    public List<MarketPayload> getHistory(
            @RequestParam(defaultValue = "200") int limit) {
        return historyBuffer.latest(limit);
    }

    @GetMapping("/history/all")
    public List<MarketPayload> getAll() {
        return historyBuffer.all();
    }
}
