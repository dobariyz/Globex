package ca.sheridancollege.dobariyz.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.sheridancollege.dobariyz.beans.ApiCallUsage;
import ca.sheridancollege.dobariyz.repositories.ApiCallUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiCallBudgetService {

    private final ApiCallUsageRepository apiCallUsageRepository;

    @Value("${marketdata.api.daily-call-limit:800}")
    private int dailyCallLimit;

    @Value("${marketdata.api.timezone:America/Toronto}")
    private String usageTimezone;

    @Transactional
    public synchronized boolean tryAcquireCall(String reason) {
        return tryAcquireCredits(1, reason);
    }

    @Transactional
    public synchronized boolean tryAcquireCredits(int creditCost, String reason) {
        LocalDate today = LocalDate.now(ZoneId.of(usageTimezone));
        ApiCallUsage usage = apiCallUsageRepository.findByUsageDate(today)
            .orElseGet(() -> newUsage(today));

        int currentCount = usage.getCallCount() != null ? usage.getCallCount() : 0;
        int safeCreditCost = Math.max(1, creditCost);
        if (currentCount + safeCreditCost > dailyCallLimit) {
            log.warn(
                "API credit budget exhausted for {}: {}/{} credits used. Skipping {} credits for {}.",
                today,
                currentCount,
                dailyCallLimit,
                safeCreditCost,
                reason
            );
            return false;
        }

        usage.setCallCount(currentCount + safeCreditCost);
        usage.setUpdatedAt(LocalDateTime.now());
        apiCallUsageRepository.save(usage);

        log.info(
            "API credit budget used for {}: {}/{} credits today",
            reason,
            usage.getCallCount(),
            dailyCallLimit
        );
        return true;
    }

    @Transactional(readOnly = true)
    public int getCallsUsedToday() {
        LocalDate today = LocalDate.now(ZoneId.of(usageTimezone));
        return apiCallUsageRepository.findByUsageDate(today)
            .map(ApiCallUsage::getCallCount)
            .orElse(0);
    }

    public int getCallsRemainingToday() {
        return Math.max(0, dailyCallLimit - getCallsUsedToday());
    }

    public int getDailyCallLimit() {
        return dailyCallLimit;
    }

    private ApiCallUsage newUsage(LocalDate today) {
        ApiCallUsage usage = new ApiCallUsage();
        usage.setUsageDate(today);
        usage.setCallCount(0);
        usage.setUpdatedAt(LocalDateTime.now());
        return usage;
    }
}
