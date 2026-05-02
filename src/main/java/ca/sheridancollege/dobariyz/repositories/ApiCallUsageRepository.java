package ca.sheridancollege.dobariyz.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.sheridancollege.dobariyz.beans.ApiCallUsage;

@Repository
public interface ApiCallUsageRepository extends JpaRepository<ApiCallUsage, Long> {

    Optional<ApiCallUsage> findByUsageDate(LocalDate usageDate);
}
