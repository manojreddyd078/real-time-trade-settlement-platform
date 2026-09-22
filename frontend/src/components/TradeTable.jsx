import { formatTradeStatus } from '../types/trade.js'

function TradeTable({ trades, onSelect }) {
  return (
    <div className="table-wrap">
      <table>
        <thead><tr><th>Trade</th><th>Instrument</th><th>Side</th><th>Counterparty</th><th>Notional</th><th>Settlement</th><th>Status</th></tr></thead>
        <tbody>{trades.map((trade) => (
          <tr key={trade.id} className="trade-row" onClick={() => onSelect?.(trade)}>
            <td className="trade-ref"><button type="button" className="trade-link">{trade.tradeReference}</button></td><td>{trade.instrumentCode}</td>
            <td><span className={`side side--${trade.tradeType.toLowerCase()}`}>{trade.tradeType}</span></td>
            <td>{trade.counterparty}</td>
            <td>{new Intl.NumberFormat('en-US', { style: 'currency', currency: trade.currencyCode, maximumFractionDigits: 0 }).format(trade.quantity * trade.price)}</td>
            <td>{trade.settlementDate}</td>
            <td><span className={`trade-status trade-status--${trade.status.toLowerCase()}`}>{formatTradeStatus(trade.status)}</span></td>
          </tr>
        ))}</tbody>
      </table>
    </div>
  )
}

export default TradeTable
