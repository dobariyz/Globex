package ca.sheridancollege.dobariyz.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.sheridancollege.dobariyz.beans.MarketIndex;

@Repository
public interface MarketIndexRepository extends JpaRepository<MarketIndex, Long> {

}
