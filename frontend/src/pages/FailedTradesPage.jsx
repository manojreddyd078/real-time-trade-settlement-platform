import { useCallback, useEffect, useState } from 'react'
import { getFailedTrades } from '../services/api.js'
import { formatTimestamp, formatTradeStatus } from '../types/trade.js'

function FailedTradesPage() {
  const [state, setState] = useState({ loading: true, failures: [], error: null })
  const refresh = useCallback(async () => {
    try {
      setState((current) => ({ ...current, loading: true, error: null }))
      const failures = await getFailedTrades()
      setState({ loading: false, failures, error: null })
    } catch (error) {
      setState((current) => ({ ...current, loading: false, error: error.message }))
    }
  }, [])

  useEffect(() => {
    refresh()
    const timer = window.setInterval(refresh, 10000)
    return () => window.clearInterval(timer)
  }, [refresh])

  return <section className="failures-page">
    <div className="intro"><div><p className="eyebrow">Exception management</p><h2>Failed trades</h2><p>Business failures and permanently failed events routed to the dead-letter queue.</p></div><button type="button" onClick={refresh} disabled={state.loading}>{state.loading ? 'Refreshing…' : 'Refresh'}</button></div>
    {state.error && <div className="notice notice--error" role="alert">Unable to load failed trades: {state.error}</div>}
    {!state.loading && !state.error && !state.failures.length && <div className="empty-state">No failed trades or dead-letter events require attention.</div>}
    <div className="failure-list">{state.failures.map((failure) => <article className="failure-card" key={failure.id}>
      <div className="failure-card__heading"><div><span>{failure.tradeReference || 'Unidentified trade event'}</span><small>{failure.eventType ? formatTradeStatus(failure.eventType) : 'Business processing failure'}</small></div><div className="failure-badges"><span className={`dlq-status dlq-status--${failure.dlqStatus.toLowerCase()}`}>DLQ: {formatTradeStatus(failure.dlqStatus)}</span>{failure.tradeStatus && <span className={`trade-status trade-status--${failure.tradeStatus.toLowerCase()}`}>{formatTradeStatus(failure.tradeStatus)}</span>}</div></div>
      <div className="failure-reason"><strong>Failure reason</strong><span>{failure.failureReason || 'No failure reason was supplied.'}</span></div>
      <div className="failure-grid"><div><span>Retry status</span><strong>{formatTradeStatus(failure.retryStatus)}</strong></div><div><span>Retry count</span><strong>{failure.retryCount}{failure.maxRetries ? ` / ${failure.maxRetries}` : ''}</strong></div><div><span>Failed at</span><strong>{formatTimestamp(failure.failedAt)}</strong></div><div><span>Original source</span><strong>{failure.originalTopic ? `${failure.originalTopic} · P${failure.originalPartition} · O${failure.originalOffset}` : 'Business workflow'}</strong></div><div><span>Correlation ID</span><strong>{failure.correlationId || 'Not available'}</strong></div><div><span>Trade ID</span><strong>{failure.tradeId || 'Not available'}</strong></div></div>
    </article>)}</div>
  </section>
}

export default FailedTradesPage
