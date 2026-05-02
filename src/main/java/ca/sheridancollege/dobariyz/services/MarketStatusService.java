package ca.sheridancollege.dobariyz.services;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;

import ca.sheridancollege.dobariyz.beans.MarketIndex;

@Service
public class MarketStatusService {
    
    public boolean isMarketOpen(MarketIndex index) {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(index.getTimezone()));
            DayOfWeek dayOfWeek = now.getDayOfWeek();
            
            // Weekend check
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                return false;
            }
            
            LocalTime currentTime = now.toLocalTime();
            LocalTime openTime = LocalTime.parse(index.getOpenTime());
            LocalTime closeTime = LocalTime.parse(index.getCloseTime());
            
            return currentTime.isAfter(openTime) && currentTime.isBefore(closeTime);
            
        } catch (Exception e) {
            System.err.println("Error checking market status for " + index.getName() + ": " + e.getMessage());
            return false; // Assume closed on error
        }
    }
}
