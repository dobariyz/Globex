package ca.sheridancollege.dobariyz.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.sheridancollege.dobariyz.beans.MarketData;
import ca.sheridancollege.dobariyz.beans.MarketIndex;
import ca.sheridancollege.dobariyz.config.MarketIndexCatalog;
import ca.sheridancollege.dobariyz.config.MarketIndexCatalog.MarketSeed;
import ca.sheridancollege.dobariyz.dto.LiveMarketDataDTO;
import ca.sheridancollege.dobariyz.repositories.MarketDataRepository;
import ca.sheridancollege.dobariyz.repositories.MarketIndexRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MarketDataService {

    @Autowired
    private MarketDataRepository marketDataRepository;

    @Autowired
    private MarketIndexRepository marketIndexRepository;

    @Autowired
    private TwelveDataService twelveDataService;

    @Autowired
    private MarketStatusService marketStatusService;

    public void updateMarketData() {
        log.info("Starting market data update with one-call batch fetching");

        List<MarketIndex> allIndices = marketIndexRepository.findAll().stream()
            .filter(this::isTrustedBenchmark)
            .collect(Collectors.toList());

        if (allIndices.isEmpty()) {
            log.warn("No indices found to update");
            return;
        }

        Set<Long> indexesWithStoredData = marketDataRepository.findLatestDataPerIndex().stream()
            .map(MarketData::getIndexId)
            .collect(Collectors.toSet());
        boolean hasStoredDataForAllTrusted = allIndices.stream()
            .map(MarketIndex::getId)
            .allMatch(indexesWithStoredData::contains);
        boolean anyMarketOpen = allIndices.stream().anyMatch(marketStatusService::isMarketOpen);

        if (hasStoredDataForAllTrusted && !anyMarketOpen) {
            log.info("All tracked markets are currently closed. Keeping stored data and saving API budget.");
            return;
        }

        List<String> symbols = allIndices.stream()
            .map(MarketIndex::getSymbol)
            .distinct()
            .collect(Collectors.toList());

        log.info("Refreshing {} trusted symbols in a single API call: {}", symbols.size(), symbols);

        Map<String, Map<String, Object>> batchQuotes = twelveDataService.fetchBatchQuotes(symbols);

        if (batchQuotes == null) {
            log.error("Batch fetch returned null. Keeping the latest stored data for this cycle.");
            return;
        }

        log.info("Received data for {} symbols in this batch out of {} total tracked symbols", batchQuotes.size(), allIndices.size());

        int updated = 0;
        int skipped = 0;

        for (MarketIndex index : allIndices) {
            try {
                Map<String, Object> quote = batchQuotes.get(index.getSymbol());

                if (quote != null && quote.containsKey("price")) {
                    boolean isOpen = marketStatusService.isMarketOpen(index);
                    if (saveMarketData(index, quote, isOpen)) {
                        updated++;
                        log.debug("Updated {}: ${}", index.getSymbol(), quote.get("price"));
                    } else {
                        skipped++;
                    }
                } else {
                    skipped++;
                    log.warn("No data received for: {} ({})", index.getName(), index.getSymbol());
                }
            } catch (Exception e) {
                skipped++;
                log.error("Failed to process {}: {}", index.getSymbol(), e.getMessage());
            }
        }

        log.info(
            "Update complete: {} updated, {} skipped in this cycle.",
            updated,
            skipped
        );
    }

    private boolean saveMarketData(MarketIndex index, Map<String, Object> quote, boolean isOpen) {
        Double currentPrice = getDoubleValue(quote, "price");

        if (currentPrice == null || currentPrice <= 0) {
            log.warn("Invalid price for {}: {}", index.getSymbol(), currentPrice);
            return false;
        }

        if (!isPriceWithinExpectedRange(index.getSymbol(), currentPrice)) {
            log.error("Rejected suspicious price for {}: {}. Keeping previous stored value.", index.getSymbol(), currentPrice);
            return false;
        }

        MarketData data = new MarketData();
        data.setIndexId(index.getId());
        data.setTimestamp(LocalDateTime.now());
        data.setIsMarketOpen(isOpen);

        Double openPrice = getDoubleValue(quote, "open");
        Double previousClose = getDoubleValue(quote, "previousClose");

        data.setCurrentPrice(currentPrice);
        data.setOpenPrice(openPrice != null ? openPrice : currentPrice);

        if (previousClose != null && previousClose > 0) {
            double change = ((currentPrice - previousClose) / previousClose) * 100;
            data.setPercentChange(change);

            log.debug(
                "   {} - Price: ${}, Change: {}%",
                index.getSymbol(),
                String.format("%.2f", currentPrice),
                String.format("%.2f", change)
            );
        } else {
            data.setPercentChange(0.0);
            log.debug(
                "   {} - Price: ${}, No previous close data",
                index.getSymbol(),
                String.format("%.2f", currentPrice)
            );
        }

        marketDataRepository.save(data);
        return true;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                log.error("Cannot parse '{}' as double for key: {}", value, key);
                return null;
            }
        }
        return null;
    }

    public List<LiveMarketDataDTO> getLiveMarketData() {
        List<MarketData> latestData = marketDataRepository.findLatestDataPerIndex();

        Map<Long, MarketIndex> indexMap = marketIndexRepository.findAll()
            .stream()
            .collect(Collectors.toMap(MarketIndex::getId, idx -> idx));

        Map<String, LiveMarketDataDTO> liveByCountry = new LinkedHashMap<>();

        latestData.stream()
            .map(data -> {
                MarketIndex index = indexMap.get(data.getIndexId());
                if (index == null || !isTrustedBenchmark(index)) {
                    return null;
                }

                return new LiveMarketDataDTO(
                    data.getId(),
                    data.getIndexId(),
                    data.getOpenPrice() != null ? BigDecimal.valueOf(data.getOpenPrice()) : BigDecimal.ZERO,
                    data.getCurrentPrice() != null ? BigDecimal.valueOf(data.getCurrentPrice()) : BigDecimal.ZERO,
                    data.getPercentChange() != null ? BigDecimal.valueOf(data.getPercentChange()) : BigDecimal.ZERO,
                    data.getIsMarketOpen(),
                    data.getTimestamp(),
                    index.getSymbol(),
                    index.getName(),
                    index.getCountryCode(),
                    index.getCountry(),
                    index.getLatitude() != null ? index.getLatitude() : 0.0,
                    index.getLongitude() != null ? index.getLongitude() : 0.0
                );
            })
            .filter(dto -> dto != null)
            .forEach(dto -> liveByCountry.putIfAbsent(dto.getCountryCode(), dto));

        return new ArrayList<>(liveByCountry.values());
    }

    private boolean isTrustedBenchmark(MarketIndex index) {
        MarketSeed seed = MarketIndexCatalog.BY_COUNTRY_CODE.get(index.getCountryCode());
        return seed != null && seed.symbol().equalsIgnoreCase(index.getSymbol());
    }

    private boolean isPriceWithinExpectedRange(String symbol, Double price) {
        MarketSeed seed = MarketIndexCatalog.BY_SYMBOL.get(symbol);
        if (seed == null) {
            return false;
        }

        return price >= seed.expectedMin() && price <= seed.expectedMax();
    }
}
