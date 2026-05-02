package ca.sheridancollege.dobariyz.beans;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "market_index")
public class MarketIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;           // "S&P 500"
    private String symbol;         // "^GSPC"
    private String country;        // "United States"
    private String timezone;       // "America/New_York"
    private String openTime;       // "09:30"
    private String closeTime;      // "16:00"

    // NEW FIELDS for globe visualization
    private Double latitude;       // 40.7128
    private Double longitude;      // -74.0060
    private String countryCode;    // "USA"

    private Double cachedOpenPrice;
    private LocalDate lastOpenDate;
}
