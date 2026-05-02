package ca.sheridancollege.dobariyz.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import ca.sheridancollege.dobariyz.beans.MarketIndex;
import ca.sheridancollege.dobariyz.config.MarketIndexCatalog.MarketSeed;
import ca.sheridancollege.dobariyz.repositories.MarketIndexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketIndexSeeder implements CommandLineRunner {

    private final MarketIndexRepository marketIndexRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        syncMarketIndices();
    }

    private void syncMarketIndices() {
        // CRITICAL: Sync sequence BEFORE doing anything else
        syncPrimaryKeySequence();
        
        List<MarketIndex> existingIndices = marketIndexRepository.findAll();
        Map<String, MarketIndex> existingByCountryCode = new HashMap<>();
        Map<String, MarketIndex> existingByCountry = new HashMap<>();
        Map<String, MarketIndex> existingBySymbol = new HashMap<>();

        for (MarketIndex index : existingIndices) {
            if (index.getCountryCode() != null && !index.getCountryCode().isBlank()) {
                existingByCountryCode.put(index.getCountryCode().toUpperCase(Locale.ROOT), index);
            }
            if (index.getCountry() != null && !index.getCountry().isBlank()) {
                existingByCountry.put(index.getCountry().toLowerCase(Locale.ROOT), index);
            }
            if (index.getSymbol() != null && !index.getSymbol().isBlank()) {
                existingBySymbol.put(index.getSymbol().toUpperCase(Locale.ROOT), index);
            }
        }

        List<MarketIndex> toSave = new ArrayList<>();
        int created = 0;
        int updated = 0;

        for (MarketSeed seed : MarketIndexCatalog.SEEDS) {
            String countryCode = seed.countryCode().toUpperCase(Locale.ROOT);
            MarketIndex index = existingByCountryCode.get(countryCode);

            if (index == null) {
                index = existingByCountry.get(seed.country().toLowerCase(Locale.ROOT));
            }
            if (index == null) {
                index = existingBySymbol.get(seed.symbol().toUpperCase(Locale.ROOT));
            }
            if (index == null) {
                // Create new entity WITHOUT setting ID - let JPA/Hibernate handle it
                index = new MarketIndex();
                created++;
            } else {
                updated++;
            }

            // The seed list is the source of truth for the primary benchmark per country.
            index.setSymbol(seed.symbol());
            index.setName(seed.name());
            index.setCountry(seed.country());
            index.setTimezone(seed.timezone());
            index.setOpenTime(seed.openTime());
            index.setCloseTime(seed.closeTime());
            index.setLatitude(seed.latitude());
            index.setLongitude(seed.longitude());
            index.setCountryCode(countryCode);

            toSave.add(index);
        }

        marketIndexRepository.saveAll(toSave);
        log.info(
            "Synced benchmark market indices: {} created, {} updated, {} total tracked after sync",
            created,
            updated,
            marketIndexRepository.count()
        );
    }

    private void syncPrimaryKeySequence() {
        try {
            Long maxId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(id), 0) FROM market_index", 
                Long.class
            );
            
            jdbcTemplate.execute(
                "SELECT setval('market_index_id_seq', " + (maxId + 1) + ", false)"
            );
            
            log.info("Synced market_index sequence to {}", maxId + 1);
        } catch (Exception e) {
            log.error("Failed to sync sequence", e);
            throw e;
        }
    }

}
