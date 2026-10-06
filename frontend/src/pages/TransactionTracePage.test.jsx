import React from 'react'
import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import TransactionTracePage from './TransactionTracePage.jsx'
import { getTransactionTrace } from '../services/api.js'

vi.mock('../services/api.js', () => ({ getTransactionTrace: vi.fn() }))

describe('TransactionTracePage', () => {
  it('displays the event and status timeline returned by the trace API', async () => {
    getTransactionTrace.mockResolvedValue({
      tradeId: 'trade-42', tradeReference: 'TRD-FLOW-1001', currentStatus: 'SETTLED',
      correlationIds: ['flow-correlation-42'], eventIds: ['event-1', 'event-2'],
      startedAt: '2026-10-06T12:00:00Z', lastUpdatedAt: '2026-10-06T12:00:05Z',
      timeline: [
        { category: 'STATUS', stage: 'validation', status: 'VALIDATED', timestamp: '2026-10-06T12:00:01Z', eventId: 'event-1', correlationId: 'flow-correlation-42', description: 'Trade validation completed', metadata: {} },
        { category: 'SETTLEMENT', stage: 'settlement', status: 'SETTLED', timestamp: '2026-10-06T12:00:05Z', eventId: 'event-2', correlationId: 'flow-correlation-42', description: 'Settlement completed', metadata: { instructionReference: 'STL-42' } }
      ]
    })
    render(<TransactionTracePage />)

    fireEvent.change(screen.getByLabelText('Trade ID'), { target: { value: 'trade-42' } })
    fireEvent.click(screen.getByRole('button', { name: 'Trace transaction' }))

    expect(await screen.findByText('TRD-FLOW-1001')).toBeInTheDocument()
    expect(screen.getByText('Trade validation completed')).toBeInTheDocument()
    expect(screen.getByText('Settlement completed')).toBeInTheDocument()
    expect(screen.getByText('flow-correlation-42')).toBeInTheDocument()
    expect(screen.getByText('STL-42')).toBeInTheDocument()
  })
})
