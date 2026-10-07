def enrich_trades(trades, instruments):
    return trades.join(instruments, "instrument_code", "left")
