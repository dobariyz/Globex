package ca.sheridancollege.dobariyz.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwelveDataService {

    @Value("${twelvedata.api.key}")
    private String apiKey;

    @Value("${marketdata.api.batch-credit-cost:50}")
    private int batchCreditCost;

    @Value("${marketdata.api.rate-limit-cooldown-minutes:720}")
    private long rateLimitCooldownMinutes;

    private final ApiCallBudgetService apiCallBudgetService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private LocalDateTime providerRateLimitedUntil;

    public Double getCurrentPrice(String symbol) {
        try {
            if (!apiCallBudgetService.tryAcquireCall("price:" + symbol)) {
                return null;
            }

            Map<String, Object> response = restTemplate.getForObject(buildUrl("price", symbol), Map.class);

            if (response != null && response.containsKey("price")) {
                return getDoubleValue(response, "price");
            }

            return null;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error fetching price for {}: {}", symbol, e.getStatusCode());
            return null;
        } catch (Exception e) {
            log.error("Error fetching price for {}: {}", symbol, e.getMessage());
            return null;
        }
    }

    public Map<String, Object> fetchQuote(String symbol) {
        try {
            if (!apiCallBudgetService.tryAcquireCall("quote:" + symbol)) {
                return null;
            }

            Map<String, Object> response = restTemplate.getForObject(buildUrl("quote", symbol), Map.class);

            if (response == null) {
                log.error("Null response for {}", symbol);
                return null;
            }

            if (response.containsKey("status") && "error".equals(response.get("status"))) {
                log.error("API error for {}: {}", symbol, response.get("message"));
                return null;
            }

            if (!response.containsKey("close")) {
                log.error("No close price in response for {}", symbol);
                return null;
            }

            return mapQuoteResponse(response);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                log.error("Rate limit exceeded for {}", symbol);
            } else {
                log.error("HTTP error for {}: {}", symbol, e.getStatusCode());
            }
            return null;
        } catch (HttpServerErrorException e) {
            log.error("Server error for {}: {}", symbol, e.getStatusCode());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching quote for {}: {}", symbol, e.getMessage());
            return null;
        }
    }

    public Map<String, Map<String, Object>> fetchBatchQuotes(List<String> symbols) {
        try {
            if (symbols == null || symbols.isEmpty()) {
                log.warn("Empty symbols list provided to batch fetch");
                return new HashMap<>();
            }

            if (isProviderRateLimited()) {
                log.warn("Skipping Twelve Data batch fetch because provider rate limit cooldown is active until {}", providerRateLimitedUntil);
                return new HashMap<>();
            }

            int creditCost = Math.max(batchCreditCost, symbols.size());
            if (!apiCallBudgetService.tryAcquireCredits(creditCost, "batch quote:" + symbols.size() + " symbols")) {
                log.warn("Skipping Twelve Data batch fetch because the local API credit budget is exhausted.");
                return new HashMap<>();
            }

            String joinedSymbols = String.join(",", symbols);
            String url = buildUrl("quote", joinedSymbols);
            log.info("Fetching {} symbols from Twelve Data in one batch request", symbols.size());

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                log.error("Null response from batch fetch");
                return null;
            }

            logRawResponse(response);

            if (response.containsKey("status") && "error".equals(response.get("status"))) {
                handleApiError(response);
                return null;
            }

            Map<String, Map<String, Object>> result = new HashMap<>();

            for (String symbol : symbols) {
                Object data = response.get(symbol);

                if (data == null) {
                    log.debug("No data for symbol: {}", symbol);
                    continue;
                }

                if (data instanceof Map<?, ?> symbolDataRaw) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> symbolData = (Map<String, Object>) symbolDataRaw;

                    if (symbolData.containsKey("status") && "error".equals(symbolData.get("status"))) {
                        log.debug("Error for {}: {}", symbol, symbolData.get("message"));
                        continue;
                    }

                    Double close = getDoubleValue(symbolData, "close");
                    if (close != null) {
                        result.put(symbol, mapQuoteResponse(symbolData));
                        log.debug("Parsed {}: ${}", symbol, close);
                    } else {
                        log.debug("No close price for {}", symbol);
                    }
                } else {
                    log.debug("Invalid data format for {}: {}", symbol, data.getClass().getName());
                }
            }

            if (result.isEmpty()) {
                log.warn("Twelve Data batch response contained no usable quotes for {} requested symbols", symbols.size());
            } else {
                log.info("Successfully parsed {} out of {} symbols", result.size(), symbols.size());
            }
            return result;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                activateProviderRateLimitCooldown();
                log.warn("Twelve Data rate limit exceeded during batch fetch. Cooling down until {}", providerRateLimitedUntil);
            } else {
                log.error("HTTP error during batch fetch: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            }
            return null;
        } catch (Exception e) {
            log.error("Batch fetch error: {}", e.getMessage(), e);
            return null;
        }
    }

    private Map<String, Object> mapQuoteResponse(Map<String, Object> response) {
        Map<String, Object> result = new HashMap<>();

        Double currentPrice = getDoubleValue(response, "close");
        Double openPrice = getDoubleValue(response, "open");
        Double previousClose = getDoubleValue(response, "previous_close");

        result.put("price", currentPrice);
        result.put("open", openPrice != null ? openPrice : currentPrice);
        result.put("previousClose", previousClose != null ? previousClose : currentPrice);
        result.put("high", getDoubleValue(response, "high"));
        result.put("low", getDoubleValue(response, "low"));
        result.put("volume", getDoubleValue(response, "volume"));

        return result;
    }

    private void logRawResponse(Map<String, Object> response) {
        try {
            String jsonResponse = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(response);
            log.debug("Raw API response:\n{}", jsonResponse);
        } catch (Exception e) {
            log.warn("Could not serialize response for debugging: {}", e.getMessage());
        }
    }

    private void handleApiError(Map<String, Object> response) {
        Object code = response.get("code");
        Object message = response.get("message");

        if ("429".equals(String.valueOf(code))) {
            activateProviderRateLimitCooldown();
            log.warn("Twelve Data rate limit exceeded. Cooling down until {}. Message: {}", providerRateLimitedUntil, message);
            return;
        }

        log.error("Twelve Data API returned error code {}: {}", code, message);
    }

    private boolean isProviderRateLimited() {
        return providerRateLimitedUntil != null && LocalDateTime.now().isBefore(providerRateLimitedUntil);
    }

    private void activateProviderRateLimitCooldown() {
        providerRateLimitedUntil = LocalDateTime.now().plusMinutes(rateLimitCooldownMinutes);
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        if (value instanceof String text) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                log.warn("Could not parse '{}' as Double for key: {}", value, key);
                return null;
            }
        }

        return null;
    }

    private String buildUrl(String endpoint, String symbol) {
        return UriComponentsBuilder
            .fromUriString("https://api.twelvedata.com/" + endpoint)
            .queryParam("symbol", symbol)
            .queryParam("apikey", apiKey)
            .build()
            .toUriString();
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }
}
