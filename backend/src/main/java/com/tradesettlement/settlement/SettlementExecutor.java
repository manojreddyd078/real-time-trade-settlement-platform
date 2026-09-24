package com.tradesettlement.settlement;

import com.tradesettlement.entity.Settlement;
import com.tradesettlement.entity.Trade;

public interface SettlementExecutor {
    void execute(Trade trade, Settlement settlement);
}
