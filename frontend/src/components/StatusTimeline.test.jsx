import React from 'react'
import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import StatusTimeline from './StatusTimeline.jsx'
import { getTradeStatusHistory } from '../services/api.js'

vi.mock('../services/api.js', () => ({ getTradeStatusHistory: vi.fn() }))

describe('StatusTimeline', () => {
  it('renders the complete status history and retry state', async () => {
    getTradeStatusHistory.mockResolvedValue({
      currentStatus: 'SETTLED',
      retry: { retryCount: 2, maxRetries: 3, status: 'RECOVERED', lastFailure: 'Temporary database timeout', lastAttemptAt: '2026-10-06T12:00:02Z' },
      history: [
        { fromStatus: null, toStatus: 'RECEIVED', reason: 'Trade received', changedAt: '2026-10-06T12:00:00Z', changedBy: 'request-1' },
        { fromStatus: 'RECEIVED', toStatus: 'VALIDATED', reason: 'Trade validation completed', changedAt: '2026-10-06T12:00:01Z', changedBy: 'system' },
        { fromStatus: 'VALIDATED', toStatus: 'ENRICHED', reason: 'Reference data enriched', changedAt: '2026-10-06T12:00:02Z', changedBy: 'system' },
        { fromStatus: 'ENRICHED', toStatus: 'ELIGIBLE', reason: 'Settlement eligibility confirmed', changedAt: '2026-10-06T12:00:03Z', changedBy: 'system' },
        { fromStatus: 'ELIGIBLE', toStatus: 'SETTLEMENT_PENDING', reason: 'Settlement processing started', changedAt: '2026-10-06T12:00:04Z', changedBy: 'system' },
        { fromStatus: 'SETTLEMENT_PENDING', toStatus: 'SETTLED', reason: 'Settlement completed', changedAt: '2026-10-06T12:00:05Z', changedBy: 'system' }
      ]
    })

    render(<StatusTimeline tradeId="flow-trade-1" />)

    expect(await screen.findByText('Settlement completed')).toBeInTheDocument()
    expect(screen.getAllByText('Settled').length).toBeGreaterThan(0)
    expect(screen.getByText('Recovered · 2 retries')).toBeInTheDocument()
    expect(screen.getByRole('status')).toHaveTextContent('Temporary database timeout')
    expect(screen.getAllByText('Initial status').length).toBe(1)
  })
})
