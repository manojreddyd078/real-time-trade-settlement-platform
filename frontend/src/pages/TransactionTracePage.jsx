import { useState } from 'react'
import { getTransactionTrace } from '../services/api.js'
import { formatDuration, formatTimestamp, formatTradeStatus } from '../types/trade.js'

function TransactionTracePage() {
  const [tradeId, setTradeId] = useState('')
  const [state, setState] = useState({ status: 'idle', trace: null })

  const submit = async (event) => {
    event.preventDefault()
    if (!tradeId.trim()) return
    setState((current) => ({ ...current, status: 'loading', error: null }))
    try { setState({ status: 'success', trace: await getTransactionTrace(tradeId.trim()), error: null }) }
    catch (error) { setState({ status: 'error', trace: null, error: error.message }) }
  }

  const trace = state.trace
  return <section className="trace-page">
    <div className="intro"><div><p className="eyebrow">End-to-end observability</p><h2>Transaction trace</h2><p>Follow status, event, retry, settlement, and dead-letter activity in timestamp order.</p></div></div>
    <form className="trace-search" onSubmit={submit}><label><span>Trade ID</span><input value={tradeId} onChange={(event) => setTradeId(event.target.value)} placeholder="Enter trade UUID" aria-label="Trade ID" /></label><button type="submit" disabled={state.status === 'loading' || !tradeId.trim()}>{state.status === 'loading' ? 'Tracing…' : 'Trace transaction'}</button></form>
    {state.error && <div className="notice notice--error" role="alert">{state.error}</div>}
    {state.status === 'idle' && <div className="empty-state">Enter a generated trade ID to load its complete transaction trace.</div>}
    {trace && <>
      <section className="trace-summary"><div><span>Trade reference</span><strong>{trace.tradeReference}</strong></div><div><span>Current status</span><strong>{formatTradeStatus(trace.currentStatus)}</strong></div><div><span>Duration</span><strong>{formatDuration(trace.startedAt, trace.lastUpdatedAt)}</strong></div><div><span>Trace entries</span><strong>{trace.timeline.length}</strong></div></section>
      <section className="trace-identifiers"><div><strong>Trade ID</strong><code>{trace.tradeId}</code></div><div><strong>Correlation IDs</strong><div>{trace.correlationIds.length ? trace.correlationIds.map((id) => <code key={id}>{id}</code>) : <span>None recorded</span>}</div></div><div><strong>Event IDs</strong><div>{trace.eventIds.length ? trace.eventIds.map((id) => <code key={id}>{id}</code>) : <span>None recorded</span>}</div></div></section>
      <ol className="trace-timeline">{trace.timeline.map((entry, index) => <li className={`trace-entry trace-entry--${entry.category.toLowerCase()}`} key={`${entry.category}-${entry.timestamp}-${entry.eventId || index}`}>
        <span className="trace-entry__marker" aria-hidden="true" />
        <div className="trace-entry__content"><div className="trace-entry__heading"><div><span className="trace-category">{entry.category}</span><strong>{formatTradeStatus(entry.status)}</strong></div><time dateTime={entry.timestamp}>{formatTimestamp(entry.timestamp)}</time></div><p>{entry.description || `${entry.category} activity recorded`}</p><div className="trace-entry__ids"><span>Stage: {entry.stage}</span>{entry.eventId && <span>Event: {entry.eventId}</span>}{entry.correlationId && <span>Correlation: {entry.correlationId}</span>}</div>{Object.keys(entry.metadata || {}).length > 0 && <dl>{Object.entries(entry.metadata).map(([key, value]) => <div key={key}><dt>{formatTradeStatus(key)}</dt><dd>{value || '—'}</dd></div>)}</dl>}</div>
      </li>)}</ol>
    </>}
  </section>
}

export default TransactionTracePage
