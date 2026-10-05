package com.tradesettlement.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import com.tradesettlement.dto.TradeSubmissionRequest;
import com.tradesettlement.entity.TradeType;
import com.tradesettlement.exception.DuplicateTradeException;
import com.tradesettlement.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradeSubmissionServiceTest {
    @Test
    void convertsConcurrentDatabaseConstraintFailureToDuplicateTrade() {
        TradeRepository trades = mock(TradeRepository.class);
        when(trades.findByTradeReference("TRD-NEW")).thenReturn(Optional.empty());
        when(trades.findByBusinessKey(anyString())).thenReturn(Optional.empty());
        when(trades.saveAndFlush(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new DataIntegrityViolationException("unique constraint"));
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        TradeStatusLifecycleService lifecycle = mock(TradeStatusLifecycleService.class);

        TradeSubmissionService service = new TradeSubmissionService(trades, publisher, lifecycle);

        assertThrows(DuplicateTradeException.class, () -> service.submit(request()));
        verify(publisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
        verify(lifecycle, never()).recordInitial(org.mockito.ArgumentMatchers.any(), anyString());
    }

    @Test
    void propagatesUnexpectedDatabaseFailureWithoutPublishingAnEvent() {
        TradeRepository trades = mock(TradeRepository.class);
        when(trades.findByTradeReference("TRD-NEW")).thenThrow(new IllegalStateException("database offline"));
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

        TradeSubmissionService service = new TradeSubmissionService(trades, publisher,
                mock(TradeStatusLifecycleService.class));

        assertThrows(IllegalStateException.class, () -> service.submit(request()));
        verify(publisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsSameBusinessTradeWithDifferentTradeReference() {
        TradeRepository trades = mock(TradeRepository.class);
        when(trades.findByTradeReference("TRD-NEW")).thenReturn(Optional.empty());
        when(trades.findByBusinessKey(anyString())).thenReturn(Optional.of(new com.tradesettlement.entity.Trade()));
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        TradeSubmissionService service = new TradeSubmissionService(trades, publisher,
                mock(TradeStatusLifecycleService.class));

        assertThrows(DuplicateTradeException.class, () -> service.submit(request()));

        verify(trades, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
        verify(publisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void normalizesEquivalentExternalReferencesIntoSameBusinessKey() {
        TradeSubmissionRequest first = request(); first.setExternalReference(" oms-42 ");
        TradeSubmissionRequest second = request(); second.setExternalReference("OMS-42");
        org.junit.jupiter.api.Assertions.assertEquals(TradeBusinessKey.from(first), TradeBusinessKey.from(second));
    }

    private TradeSubmissionRequest request() {
        TradeSubmissionRequest value = new TradeSubmissionRequest();
        value.setTradeReference("TRD-NEW"); value.setTradeType(TradeType.BUY);
        value.setInstrumentId(UUID.fromString("ac38d8e2-6e41-4458-b901-43851cbf19e6"));
        value.setBuyerCounterpartyId(UUID.fromString("4b5588ed-c391-4079-b14c-34c49a21949a"));
        value.setSellerCounterpartyId(UUID.fromString("5e884423-f880-4534-89d2-526542164975"));
        value.setQuantity(new BigDecimal("1000.0000")); value.setPrice(new BigDecimal("125.75000000"));
        value.setCurrencyCode("USD"); value.setTradeDate(LocalDate.parse("2026-09-29"));
        value.setSettlementDate(LocalDate.parse("2026-10-01")); return value;
    }
}
