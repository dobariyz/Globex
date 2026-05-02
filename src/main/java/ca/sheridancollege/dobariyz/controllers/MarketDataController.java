package ca.sheridancollege.dobariyz.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.sheridancollege.dobariyz.dto.LiveMarketDataDTO;
import ca.sheridancollege.dobariyz.services.ApiCallBudgetService;
import ca.sheridancollege.dobariyz.services.MarketDataService;

@RestController
@RequestMapping("/api/markets")
@CrossOrigin(origins = "http://localhost:5173")
public class MarketDataController {
    
    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private ApiCallBudgetService apiCallBudgetService;
    
    @GetMapping("/test-run")
    public String testRun() {
        marketDataService.updateMarketData();
        return "Triggered!";
    }
    
    @GetMapping("/live")
    public ResponseEntity<List<LiveMarketDataDTO>> getLiveData() {
        List<LiveMarketDataDTO> data = marketDataService.getLiveMarketData();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/call-budget")
    public ResponseEntity<Map<String, Integer>> getCallBudget() {
        return ResponseEntity.ok(Map.of(
            "dailyLimit", apiCallBudgetService.getDailyCallLimit(),
            "usedToday", apiCallBudgetService.getCallsUsedToday(),
            "remainingToday", apiCallBudgetService.getCallsRemainingToday()
        ));
    }
}
