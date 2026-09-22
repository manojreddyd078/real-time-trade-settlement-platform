package com.tradesettlement.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.tradesettlement.entity.Counterparty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounterpartyRepository extends JpaRepository<Counterparty, UUID> {
    List<Counterparty> findAllByOrderByCounterpartyCodeAsc();
    Optional<Counterparty> findByIdAndActiveTrue(UUID id);
}
