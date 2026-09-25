package com.tradesettlement.service;

import java.util.Optional;
import java.util.UUID;

import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.entity.TradeType;
import com.tradesettlement.exception.TradeProcessingException;
import com.tradesettlement.kafka.TradeEvent;
import com.tradesettlement.kafka.TradeEventType;
import com.tradesettlement.repository.TradeRepository;
import com.tradesettlement.validation.TradeProcessingValidator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradeProcessingServiceTest {

    @Test
    void advancesStatusAndEmitsValidationCompletedEvent() {
        TradeRepository repository = mock(TradeRepository.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        TradeProcessingValidator validator = mock(TradeProcessingValidator.class);
        TradeProcessingService service = new TradeProcessingService(repository, eventPublisher, validator, lifecycle());
        Trade trade = trade(TradeStatus.RECEIVED);
        TradeEvent event = TradeEvent.accepted(trade, "correlation-123");
        when(repository.findById(trade.getId())).thenReturn(Optional.of(trade));

        service.processAcceptedTrade(event);

        assertEquals(TradeStatus.VALIDATED, trade.getStatus());
        verify(repository).saveAndFlush(trade);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        TradeEvent publishedEvent = (TradeEvent) eventCaptor.getValue();
        assertEquals(TradeEventType.VALIDATION_COMPLETED, publishedEvent.getEventType());
        assertEquals(trade.getId(), publishedEvent.getTradeId());
        assertEquals(TradeStatus.VALIDATED, publishedEvent.getStatus());
        assertEquals("correlation-123", publishedEvent.getCorrelationId());
    }

    @Test
    void treatsAnAlreadyValidatedTradeAsIdempotentlyProcessed() {
        TradeRepository repository = mock(TradeRepository.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        TradeProcessingValidator validator = mock(TradeProcessingValidator.class);
        TradeProcessingService service = new TradeProcessingService(repository, eventPublisher, validator, lifecycle());
        Trade trade = trade(TradeStatus.VALIDATED);
        TradeEvent event = TradeEvent.accepted(trade, "correlation-123");
        when(repository.findById(trade.getId())).thenReturn(Optional.of(trade));

        service.processAcceptedTrade(event);

        verify(repository, never()).saveAndFlush(trade);
        verify(eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsAnEventForAnUnknownTrade() {
        TradeRepository repository = mock(TradeRepository.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        TradeProcessingValidator validator = mock(TradeProcessingValidator.class);
        TradeProcessingService service = new TradeProcessingService(repository, eventPublisher, validator, lifecycle());
        Trade trade = trade(TradeStatus.RECEIVED);
        TradeEvent event = TradeEvent.accepted(trade, "correlation-123");
        when(repository.findById(trade.getId())).thenReturn(Optional.empty());

        assertThrows(TradeProcessingException.class, () -> service.processAcceptedTrade(event));
    }

    @Test
    void rejectsInvalidTradeAndPublishesValidationErrors() {
        TradeRepository repository = mock(TradeRepository.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        TradeProcessingValidator validator = mock(TradeProcessingValidator.class);
        TradeProcessingService service = new TradeProcessingService(repository, eventPublisher, validator, lifecycle());
        Trade trade = trade(TradeStatus.RECEIVED);
        TradeEvent event = TradeEvent.accepted(trade, "correlation-123");
        when(repository.findById(trade.getId())).thenReturn(Optional.of(trade));
        when(validator.validate(trade)).thenReturn(java.util.List.of("currency is missing or inactive"));

        service.processAcceptedTrade(event);

        assertEquals(TradeStatus.REJECTED, trade.getStatus());
        verify(repository).saveAndFlush(trade);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        TradeEvent publishedEvent = (TradeEvent) eventCaptor.getValue();
        assertEquals(TradeEventType.VALIDATION_REJECTED, publishedEvent.getEventType());
        assertEquals(java.util.List.of("currency is missing or inactive"), publishedEvent.getValidationErrors());
    }

    private Trade trade(TradeStatus status) {
        Trade trade = new Trade();
        ReflectionTestUtils.setField(trade, "id", UUID.randomUUID());
        trade.setTradeReference("TRD-2026-12001");
        trade.setTradeType(TradeType.BUY);
        trade.setStatus(status);
        return trade;
    }

    private TradeStatusLifecycleService lifecycle() {
        return new TradeStatusLifecycleService(mock(com.tradesettlement.repository.TradeStatusHistoryRepository.class),
                java.time.Clock.systemUTC());
    }
}
