import { useEffect, useState } from 'react'
import { getTradeDetails } from '../services/api.js'
import { formatTradeStatus } from '../types/trade.js'

function Item({ label, value }) { return <div className="detail-item"><span>{label}</span><strong>{value || 'Pending enrichment'}</strong></div> }

function TradeDetailsPage({ tradeId, fallback, onBack }) {
  const [state, setState] = useState({ loading: true, trade: fallback })
  useEffect(() => { getTradeDetails(tradeId).then((trade) => setState({ loading: false, trade })).catch((error) => setState({ loading: false, trade: fallback, error: error.message })) }, [tradeId, fallback])
  const trade = state.trade
  if (!trade) return <section className="submission-page"><button className="back-link" onClick={onBack}>← Back to overview</button><div className="notice notice--error">{state.error || 'Trade details are unavailable.'}</div></section>
  return <section className="submission-page"><button className="back-link" type="button" onClick={onBack}>← Back to overview</button><div className="page-heading"><p className="eyebrow">Trade details</p><h2>{trade.tradeReference}</h2><p>Captured economics and enriched reference information.</p></div>{state.error && <div className="notice notice--pending">Showing cached details: {state.error}</div>}<div className="details-card"><div className="details-header"><span className={`trade-status trade-status--${trade.status.toLowerCase()}`}>{formatTradeStatus(trade.status)}</span>{state.loading && <small>Refreshing…</small>}</div><section className="detail-section"><h3>Trade economics</h3><div className="detail-grid"><Item label="Side" value={trade.tradeType} /><Item label="Quantity" value={trade.quantity?.toLocaleString?.() || trade.quantity} /><Item label="Price" value={trade.price} /><Item label="Currency" value={trade.currencyCode} /><Item label="Trade date" value={trade.tradeDate} /><Item label="Settlement date" value={trade.settlementDate} /></div></section><section className="detail-section"><h3>Enriched instrument</h3><div className="detail-grid"><Item label="Instrument code" value={trade.instrumentCode} /><Item label="Instrument name" value={trade.instrumentName} /></div></section><section className="detail-section"><h3>Enriched counterparties</h3><div className="detail-grid"><Item label="Buyer code" value={trade.buyerCounterpartyCode} /><Item label="Buyer name" value={trade.buyerCounterpartyName || trade.counterparty} /><Item label="Seller code" value={trade.sellerCounterpartyCode} /><Item label="Seller name" value={trade.sellerCounterpartyName} /></div></section></div></section>
}
export default TradeDetailsPage
