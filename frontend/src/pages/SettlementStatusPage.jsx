import { useCallback, useEffect, useState } from 'react'
import { getSettlements } from '../services/api.js'
import { formatTradeStatus } from '../types/trade.js'

function SettlementStatusPage() {
  const [state, setState] = useState({ loading: true, settlements: [] })
  const refresh = useCallback(async () => {
    try { setState((current) => ({ ...current, loading: true })); const settlements = await getSettlements(); setState({ loading: false, settlements }) }
    catch (error) { setState((current) => ({ ...current, loading: false, error: error.message })) }
  }, [])
  useEffect(() => { refresh(); const timer = window.setInterval(refresh, 5000); return () => window.clearInterval(timer) }, [refresh])

  return <section className="settlements-page"><div className="intro"><div><p className="eyebrow">Settlement processing</p><h2>Settlement status</h2><p>Current state of submitted settlement instructions.</p></div><button type="button" onClick={refresh} disabled={state.loading}>{state.loading ? 'Refreshing…' : 'Refresh'}</button></div>{state.error && <div className="notice notice--error" role="alert">{state.error}</div>}<div className="settlement-list">{!state.loading && !state.settlements.length && <div className="empty-state">No settlement instructions have been created yet.</div>}{state.settlements.map((settlement) => <article className="settlement-card" key={settlement.id}><div className="settlement-card__heading"><div><span>{settlement.tradeReference}</span><strong>{settlement.instructionReference}</strong></div><span className={`trade-status trade-status--${settlement.status.toLowerCase()}`}>{formatTradeStatus(settlement.status)}</span></div><div className="settlement-card__details"><span>Amount <strong>{new Intl.NumberFormat('en-US', { style: 'currency', currency: settlement.currencyCode }).format(settlement.amount)}</strong></span><span>Requested <strong>{new Date(settlement.requestedAt).toLocaleString()}</strong></span><span>Settled <strong>{settlement.settledAt ? new Date(settlement.settledAt).toLocaleString() : '—'}</strong></span></div>{settlement.failureReason && <p className="settlement-failure"><strong>Failure reason:</strong> {settlement.failureReason}</p>}</article>)}</div></section>
}
export default SettlementStatusPage
