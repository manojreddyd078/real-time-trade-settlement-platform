import { useCallback, useEffect, useState } from 'react'
import { getTradeStatusHistory } from '../services/api.js'
import { formatDuration, formatTimestamp, formatTradeStatus, isExceptionStatus } from '../types/trade.js'

function StatusTimeline({ tradeId, compact = false }) {
  const [state, setState] = useState({ loading: true, data: null, error: null })

  const refresh = useCallback(async () => {
    try {
      setState((current) => ({ ...current, loading: true, error: null }))
      const data = await getTradeStatusHistory(tradeId)
      setState({ loading: false, data, error: null })
    } catch (error) {
      setState((current) => ({ ...current, loading: false, error: error.message }))
    }
  }, [tradeId])

  useEffect(() => { refresh() }, [refresh])

  const history = state.data?.history || []
  const startedAt = history[0]?.changedAt
  const finishedAt = history.at(-1)?.changedAt
  const currentStatus = state.data?.currentStatus
  const retry = state.data?.retry
  const active = currentStatus && !isExceptionStatus(currentStatus) && currentStatus !== 'SETTLED'
  const durationEnd = active ? new Date().toISOString() : finishedAt

  useEffect(() => {
    if (!active && retry?.status !== 'RETRYING') return undefined
    const timer = window.setInterval(refresh, 5000)
    return () => window.clearInterval(timer)
  }, [active, refresh, retry?.status])

  return <section className={`status-timeline ${compact ? 'status-timeline--compact' : ''}`} aria-label="Trade status history">
    <div className="status-timeline__heading">
      <div><h3>Settlement timeline</h3><p>All recorded processing transitions</p></div>
      <div className="status-timeline__summary">
        {currentStatus && <span className={`trade-status trade-status--${currentStatus.toLowerCase()}`}>{formatTradeStatus(currentStatus)}</span>}
        {retry?.retryCount > 0 && <span className={`retry-status retry-status--${retry.status.toLowerCase()}`}>{retry.status === 'RECOVERED' ? 'Recovered' : formatTradeStatus(retry.status)} · {retry.retryCount} {retry.retryCount === 1 ? 'retry' : 'retries'}</span>}
        <strong>{history.length ? formatDuration(startedAt, durationEnd) : '—'}</strong><small>Total processing time{active ? ' so far' : ''}</small>
      </div>
    </div>
    {retry?.retryCount > 0 && <div className={`retry-banner retry-banner--${retry.status.toLowerCase()}`} role="status"><strong>Processing {retry.status === 'RETRYING' ? 'is retrying' : retry.status === 'EXHAUSTED' ? 'retries exhausted' : 'recovered after retry'}</strong><span>Retry count: {retry.retryCount}{retry.maxRetries ? ` · Configured limit per event: ${retry.maxRetries}` : ''}</span>{retry.lastFailure && <small>Last transient failure: {retry.lastFailure}{retry.lastAttemptAt ? ` · ${formatTimestamp(retry.lastAttemptAt)}` : ''}</small>}</div>}
    {state.loading && !state.data && <p className="timeline-message">Loading status history…</p>}
    {state.error && <div className="timeline-message timeline-message--error" role="alert">Unable to load status history: {state.error} <button type="button" onClick={refresh}>Retry</button></div>}
    {!state.loading && !state.error && !history.length && <p className="timeline-message">No status transitions have been recorded.</p>}
    {!!history.length && <ol className="timeline-list">
      {history.map((transition, index) => {
        const exception = isExceptionStatus(transition.toStatus)
        const previousAt = index > 0 ? history[index - 1].changedAt : null
        return <li className={exception ? 'timeline-entry timeline-entry--exception' : 'timeline-entry'} key={`${transition.toStatus}-${transition.changedAt}-${index}`}>
          <span className="timeline-entry__marker" aria-hidden="true" />
          <div className="timeline-entry__body">
            <div className="timeline-entry__title"><strong>{formatTradeStatus(transition.toStatus)}</strong><time dateTime={transition.changedAt}>{formatTimestamp(transition.changedAt)}</time></div>
            <p>{transition.reason || 'Status updated'}</p>
            <div className="timeline-entry__meta">
              <span>{transition.fromStatus ? `${formatTradeStatus(transition.fromStatus)} → ${formatTradeStatus(transition.toStatus)}` : 'Initial status'}</span>
              {previousAt && <span>Step duration: {formatDuration(previousAt, transition.changedAt)}</span>}
              {transition.changedBy && <span>By {transition.changedBy}</span>}
            </div>
            {exception && <div className="timeline-reason"><strong>{transition.toStatus === 'REJECTED' ? 'Rejection reason' : 'Failure reason'}:</strong> {transition.reason || 'No reason supplied'}</div>}
          </div>
        </li>
      })}
    </ol>}
  </section>
}

export default StatusTimeline
