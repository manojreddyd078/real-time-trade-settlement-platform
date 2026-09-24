package com.tradesettlement.settlement;

import com.tradesettlement.entity.Settlement;
import com.tradesettlement.entity.Trade;
import org.springframework.stereotype.Component;

@Component
public class LocalSettlementExecutor implements SettlementExecutor {
    @Override public void execute(Trade trade, Settlement settlement) {
        if (trade.getQuantity() == null || trade.getPrice() == null) {
            throw new IllegalStateException("Trade economics are incomplete");
        }
    }
}
