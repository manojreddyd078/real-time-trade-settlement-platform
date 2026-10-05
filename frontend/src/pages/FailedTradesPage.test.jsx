import React from 'react'
import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import FailedTradesPage from './FailedTradesPage.jsx'
import { getFailedTrades } from '../services/api.js'

vi.mock('../services/api.js', () => ({ getFailedTrades: vi.fn() }))

describe('FailedTradesPage', () => {
  it('shows loading, then renders failure, retry, and DLQ details', async () => {
    let resolveRequest
    getFailedTrades.mockReturnValue(new Promise((resolve) => { resolveRequest = resolve }))
    render(<FailedTradesPage />)

    expect(screen.getByRole('button', { name: /Refreshing/ })).toBeDisabled()
    resolveRequest([{
      id: 'failure-1', tradeReference: 'TRD-FAILED-1', eventType: 'SETTLEMENT_FAILED',
      dlqStatus: 'ROUTED', tradeStatus: 'FAILED', failureReason: 'Clearing gateway unavailable',
      retryStatus: 'EXHAUSTED', retryCount: 3, maxRetries: 3,
      failedAt: '2026-10-05T14:30:00Z', originalTopic: 'trade-settlement',
      originalPartition: 2, originalOffset: 81, correlationId: 'corr-1', tradeId: 'trade-1'
    }])

    expect(await screen.findByText('TRD-FAILED-1')).toBeInTheDocument()
    expect(screen.getByText('Clearing gateway unavailable')).toBeInTheDocument()
    expect(screen.getByText('3 / 3')).toBeInTheDocument()
    expect(screen.getByText('DLQ: Routed')).toBeInTheDocument()
    expect(screen.getByText('trade-settlement · P2 · O81')).toBeInTheDocument()
  })

  it('renders an API failure message', async () => {
    getFailedTrades.mockRejectedValue(new Error('Service unavailable'))
    render(<FailedTradesPage />)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Unable to load failed trades: Service unavailable'
    )
  })
})
