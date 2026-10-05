import React from 'react'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import TradeForm from './TradeForm.jsx'
import { submitTrade } from '../services/api.js'

vi.mock('../services/api.js', () => ({
  submitTrade: vi.fn(),
  getTrade: vi.fn()
}))

const validValues = {
  'Trade reference': 'TRD-UI-1001',
  'Instrument ID': 'ac38d8e2-6e41-4458-b901-43851cbf19e6',
  'Buyer counterparty ID': '4b5588ed-c391-4079-b14c-34c49a21949a',
  'Seller counterparty ID': '5e884423-f880-4534-89d2-526542164975',
  Quantity: '100', Price: '25.50', Currency: 'USD',
  'Trade date': '2026-10-05', 'Settlement date': '2026-10-07'
}

function fillValidTrade() {
  for (const [label, value] of Object.entries(validValues)) {
    fireEvent.change(screen.getByLabelText(label), { target: { value } })
  }
}

describe('TradeForm request states', () => {
  it('disables submission and displays a loading state while the API is pending', async () => {
    submitTrade.mockReturnValue(new Promise(() => {}))
    render(<TradeForm />)
    fillValidTrade()

    fireEvent.click(screen.getByRole('button', { name: 'Submit trade' }))

    expect(await screen.findByRole('button', { name: /Submitting/ })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Clear form' })).toBeDisabled()
  })

  it('renders API errors and request IDs', async () => {
    submitTrade.mockRejectedValue(Object.assign(new Error('Database temporarily unavailable'), {
      requestId: 'request-ui-42', fieldErrors: {}
    }))
    render(<TradeForm />)
    fillValidTrade()

    fireEvent.click(screen.getByRole('button', { name: 'Submit trade' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Trade could not be submitted')
    expect(screen.getByRole('alert')).toHaveTextContent('Database temporarily unavailable')
    expect(screen.getByRole('alert')).toHaveTextContent('Request ID: request-ui-42')
    await waitFor(() => expect(submitTrade).toHaveBeenCalledOnce())
  })
})
