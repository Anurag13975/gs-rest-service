package com.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties (ignoreUnknown = true)
public class YahooMarketPriceResponse {
    private Chart chart;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Chart {
        private List<Result> result;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Result {
            private Meta meta;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Meta {
                private Double regularMarketPrice;
            }
        }
    }
}