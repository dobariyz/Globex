package ca.sheridancollege.dobariyz.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MarketIndexCatalog {

    public static final List<MarketSeed> SEEDS = List.of(
        // North America
        seed("S&P 500", "SPY", "United States", "America/New_York", "09:30", "16:00", 38.9072, -77.0369, "USA", 100.0, 1000.0),
        seed("S&P/TSX Composite", "EWC", "Canada", "America/Toronto", "09:30", "16:00", 43.6532, -79.3832, "CAN", 10.0, 100.0),
        seed("IPC Mexico", "EWW", "Mexico", "America/Mexico_City", "08:30", "15:00", 19.4326, -99.1332, "MEX", 10.0, 150.0),
        seed("Bovespa", "EWZ", "Brazil", "America/Sao_Paulo", "10:00", "17:00", -23.5505, -46.6333, "BRA", 10.0, 100.0),
        seed("MERVAL", "ARGT", "Argentina", "America/Argentina/Buenos_Aires", "11:00", "17:00", -34.6037, -58.3816, "ARG", 10.0, 100.0),
        seed("IPSA", "ECH", "Chile", "America/Santiago", "09:30", "16:00", -33.4489, -70.6693, "CHL", 10.0, 100.0),
        seed("S&P/BVL Peru General", "EPU", "Peru", "America/Lima", "09:00", "16:00", -12.0464, -77.0428, "PER", 10.0, 150.0),
        seed("COLCAP", "GXG", "Colombia", "America/Bogota", "09:30", "16:00", 4.7110, -74.0721, "COL", 5.0, 80.0),
        
        // Western Europe
        seed("FTSE 100", "EWU", "United Kingdom", "Europe/London", "08:00", "16:30", 51.5074, -0.1278, "GBR", 10.0, 100.0),
        seed("CAC 40", "EWQ", "France", "Europe/Paris", "09:00", "17:30", 48.8566, 2.3522, "FRA", 10.0, 100.0),
        seed("DAX", "EWG", "Germany", "Europe/Berlin", "09:00", "17:30", 52.5200, 13.4050, "DEU", 10.0, 100.0),
        seed("IBEX 35", "EWP", "Spain", "Europe/Madrid", "09:00", "17:30", 40.4168, -3.7038, "ESP", 10.0, 100.0),
        seed("FTSE MIB", "EWI", "Italy", "Europe/Rome", "09:00", "17:30", 41.9028, 12.4964, "ITA", 10.0, 100.0),
        seed("AEX", "EWN", "Netherlands", "Europe/Amsterdam", "09:00", "17:30", 52.3676, 4.9041, "NLD", 10.0, 150.0),
        seed("BEL 20", "EWK", "Belgium", "Europe/Brussels", "09:00", "17:30", 50.8503, 4.3517, "BEL", 10.0, 80.0),
        seed("SMI", "EWL", "Switzerland", "Europe/Zurich", "09:00", "17:30", 46.9480, 7.4474, "CHE", 10.0, 120.0),
        seed("ATX", "EWO", "Austria", "Europe/Vienna", "09:00", "17:30", 48.2082, 16.3738, "AUT", 10.0, 80.0),
        seed("OMX Stockholm 30", "EWD", "Sweden", "Europe/Stockholm", "09:00", "17:30", 59.3293, 18.0686, "SWE", 10.0, 100.0),
        seed("OSE All Share", "NORW", "Norway", "Europe/Oslo", "09:00", "16:25", 59.9139, 10.7522, "NOR", 10.0, 80.0),
        seed("OMXC25", "EDEN", "Denmark", "Europe/Copenhagen", "09:00", "17:00", 55.6761, 12.5683, "DNK", 10.0, 80.0),
        seed("OMX Helsinki 25", "EFNL", "Finland", "Europe/Helsinki", "10:00", "18:30", 60.1699, 24.9384, "FIN", 5.0, 70.0),
        seed("PSI", "PGAL", "Portugal", "Europe/Lisbon", "08:00", "16:30", 38.7223, -9.1393, "PRT", 5.0, 70.0),
        seed("ISEQ Overall", "EIRL", "Ireland", "Europe/Dublin", "08:00", "16:30", 53.3498, -6.2603, "IRL", 10.0, 150.0),
        
        // Eastern Europe
        seed("WIG20", "EPOL", "Poland", "Europe/Warsaw", "09:00", "17:00", 52.2297, 21.0122, "POL", 10.0, 80.0),
        seed("ATHEX Composite", "GREK", "Greece", "Europe/Athens", "10:15", "17:20", 37.9838, 23.7275, "GRC", 10.0, 150.0),
        seed("BIST 100", "TUR", "Turkey", "Europe/Istanbul", "10:00", "18:00", 41.0082, 28.9784, "TUR", 10.0, 100.0),
        seed("MOEX Russia Index", "ERUS", "Russia", "Europe/Moscow", "10:00", "18:50", 55.7558, 37.6173, "RUS", 10.0, 100.0),
        
        // Africa
        seed("Top 40", "EZA", "South Africa", "Africa/Johannesburg", "09:00", "17:00", -25.7479, 28.2293, "ZAF", 10.0, 150.0),
        seed("EGX 30", "EGPT", "Egypt", "Africa/Cairo", "10:00", "14:30", 30.0444, 31.2357, "EGY", 5.0, 80.0),
        seed("NSE All Share", "AFK", "Kenya", "Africa/Nairobi", "09:30", "15:00", -1.2921, 36.8219, "KEN", 10.0, 100.0),
        
        // Middle East
        seed("Tadawul All Share", "KSA", "Saudi Arabia", "Asia/Riyadh", "10:00", "15:00", 24.7136, 46.6753, "SAU", 10.0, 120.0),
        seed("Dubai Financial Market", "UAE", "United Arab Emirates", "Asia/Dubai", "10:00", "14:45", 25.2048, 55.2708, "ARE", 5.0, 80.0),
        seed("QE Index", "QAT", "Qatar", "Asia/Qatar", "09:30", "13:15", 25.2854, 51.5310, "QAT", 10.0, 100.0),
        seed("TA-35", "EIS", "Israel", "Asia/Jerusalem", "09:45", "17:30", 31.7683, 35.2137, "ISR", 10.0, 150.0),
        
        // South Asia
        seed("Nifty 50", "INDA", "India", "Asia/Kolkata", "09:15", "15:30", 28.6139, 77.2090, "IND", 10.0, 120.0),
        seed("KSE 100", "PKR", "Pakistan", "Asia/Karachi", "09:30", "15:30", 24.8607, 67.0011, "PAK", 5.0, 80.0),
        
        // East Asia
        seed("Shanghai Composite", "ASHR", "China", "Asia/Shanghai", "09:30", "15:00", 39.9042, 116.4074, "CHN", 10.0, 120.0),
        seed("Hang Seng", "EWH", "Hong Kong", "Asia/Hong_Kong", "09:30", "16:00", 22.3193, 114.1694, "HKG", 10.0, 100.0),
        seed("Nikkei 225", "EWJ", "Japan", "Asia/Tokyo", "09:00", "15:00", 35.6762, 139.6503, "JPN", 20.0, 200.0),
        seed("KOSPI", "EWY", "South Korea", "Asia/Seoul", "09:00", "15:30", 37.5665, 126.9780, "KOR", 20.0, 200.0),
        seed("Taiwan Weighted", "EWT", "Taiwan", "Asia/Taipei", "09:00", "13:30", 25.0330, 121.5654, "TWN", 10.0, 150.0),
        
        // Southeast Asia
        seed("Straits Times", "EWS", "Singapore", "Asia/Singapore", "09:00", "17:00", 1.3521, 103.8198, "SGP", 10.0, 100.0),
        seed("FTSE Bursa Malaysia KLCI", "EWM", "Malaysia", "Asia/Kuala_Lumpur", "09:00", "17:00", 3.1390, 101.6869, "MYS", 10.0, 100.0),
        seed("SET Index", "THD", "Thailand", "Asia/Bangkok", "10:00", "16:30", 13.7563, 100.5018, "THA", 20.0, 200.0),
        seed("Jakarta Composite", "EIDO", "Indonesia", "Asia/Jakarta", "09:00", "16:00", -6.2088, 106.8456, "IDN", 10.0, 100.0),
        seed("PSEi", "EPHE", "Philippines", "Asia/Manila", "09:30", "15:30", 14.5995, 120.9842, "PHL", 5.0, 80.0),
        seed("VN-Index", "VNM", "Vietnam", "Asia/Ho_Chi_Minh", "09:00", "15:00", 10.8231, 106.6297, "VNM", 5.0, 70.0),
        
        // Oceania
        seed("ASX 200", "EWA", "Australia", "Australia/Sydney", "10:00", "16:00", -35.2809, 149.1300, "AUS", 10.0, 100.0),
        seed("S&P/NZX 50", "ENZL", "New Zealand", "Pacific/Auckland", "10:00", "16:45", -41.2866, 174.7756, "NZL", 10.0, 150.0),
        
        // Regional ETFs
        seed("EURO STOXX 50", "FEZ", "Eurozone", "Europe/Brussels", "09:00", "17:30", 50.8503, 4.3517, "EUR", 20.0, 150.0)
        
        // Removed markets (no unique ETF available on free tier):
        // - Iceland (OMXI10) - no ETF
        // - Czech Republic (PX) - no unique ETF
        // - Hungary (BUX) - no unique ETF
        // - Romania (BET) - no unique ETF
        // - Ukraine (PFTS) - no unique ETF
        // - Bangladesh (DSEX) - no unique ETF
        // - Sri Lanka (CSE All Share) - no unique ETF
        // - Morocco (MASI/GAF) - symbol not found
        // - Nigeria (NSE 30/NGE) - premium only
    );

    public static final Map<String, MarketSeed> BY_SYMBOL = SEEDS.stream()
        .collect(Collectors.toMap(MarketSeed::symbol, Function.identity()));

    public static final Map<String, MarketSeed> BY_COUNTRY_CODE = SEEDS.stream()
        .collect(Collectors.toMap(MarketSeed::countryCode, Function.identity()));

    private MarketIndexCatalog() {
    }

    private static MarketSeed seed(
        String name, String symbol, String country, String timezone, String openTime, String closeTime,
        Double latitude, Double longitude, String countryCode, Double expectedMin, Double expectedMax
    ) {
        return new MarketSeed(name, symbol, country, timezone, openTime, closeTime, latitude, longitude, countryCode, expectedMin, expectedMax);
    }

    public record MarketSeed(
        String name,
        String symbol,
        String country,
        String timezone,
        String openTime,
        String closeTime,
        Double latitude,
        Double longitude,
        String countryCode,
        Double expectedMin,
        Double expectedMax
    ) {
    }
}