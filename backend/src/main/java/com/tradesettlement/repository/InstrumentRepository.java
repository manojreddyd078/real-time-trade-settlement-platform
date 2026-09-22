package com.tradesettlement.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.tradesettlement.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentRepository extends JpaRepository<Instrument, UUID> {
    List<Instrument> findAllByOrderByInstrumentCodeAsc();
    Optional<Instrument> findByIdAndActiveTrue(UUID id);
}
