package ca.sheridancollege.dobariyz.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LiveMarketDataDTO {
    private Long id;
    private Long indexId;
    
    private BigDecimal openPrice;
    private BigDecimal currentPrice;
    private BigDecimal percentChange;
    private Boolean isMarketOpen;
    private LocalDateTime timestamp;
    
    private String indexCode;      // symbol
    private String indexName;      // name
    private String countryCode;    // country_code
    private String countryName;    // country
    private Double latitude;
    private Double longitude;
    
    // Constructors
    public LiveMarketDataDTO() {}
    
    public LiveMarketDataDTO(Long id, Long indexId, BigDecimal openPrice, 
                             BigDecimal currentPrice, BigDecimal percentChange,
                             Boolean isMarketOpen, LocalDateTime timestamp,
                             String indexCode, String indexName, String countryCode,
                             String countryName, Double latitude, Double longitude) {
        this.id = id;
        this.indexId = indexId;
        this.openPrice = openPrice;
        this.currentPrice = currentPrice;
        this.percentChange = percentChange;
        this.isMarketOpen = isMarketOpen;
        this.timestamp = timestamp;
        this.indexCode = indexCode;
        this.indexName = indexName;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getIndexId() { return indexId; }
    public void setIndexId(Long indexId) { this.indexId = indexId; }
    
    public BigDecimal getOpenPrice() { return openPrice; }
    public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }
    
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    
    public BigDecimal getPercentChange() { return percentChange; }
    public void setPercentChange(BigDecimal percentChange) { this.percentChange = percentChange; }
    
    public Boolean getIsMarketOpen() { return isMarketOpen; }
    public void setIsMarketOpen(Boolean isMarketOpen) { this.isMarketOpen = isMarketOpen; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getIndexCode() { return indexCode; }
    public void setIndexCode(String indexCode) { this.indexCode = indexCode; }
    
    public String getIndexName() { return indexName; }
    public void setIndexName(String indexName) { this.indexName = indexName; }
    
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    
    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}