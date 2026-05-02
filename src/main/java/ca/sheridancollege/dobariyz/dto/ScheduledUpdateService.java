package ca.sheridancollege.dobariyz.dto;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ca.sheridancollege.dobariyz.services.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduledUpdateService {

    private final MarketDataService marketDataService;

    @Scheduled(
        fixedDelayString = "${marketdata.refresh.delay-ms:120000}",
        initialDelayString = "${marketdata.refresh.initial-delay-ms:10000}"
    )
    public void run() {
        log.info("Scheduled market data refresh triggered");
        try {
            marketDataService.updateMarketData();
            log.info("Scheduled market data refresh completed");
        } catch (Exception e) {
            log.error("Scheduled market data refresh failed", e);
        }
    }
}
