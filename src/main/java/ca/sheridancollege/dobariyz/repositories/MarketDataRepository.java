package ca.sheridancollege.dobariyz.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ca.sheridancollege.dobariyz.beans.MarketData;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, Long> {
    
    // Get latest market data for each index (one record per index)
    @Query("""
        SELECT md FROM MarketData md
        WHERE md.timestamp = (
            SELECT MAX(md2.timestamp)
            FROM MarketData md2
            WHERE md2.indexId = md.indexId
        )
        ORDER BY md.indexId
    """)
    List<MarketData> findLatestDataPerIndex();
}
