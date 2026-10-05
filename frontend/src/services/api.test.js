import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError, getFailedTrades, submitTrade } from './api.js'

afterEach(() => vi.unstubAllGlobals())

describe('API failure handling', () => {
  it('preserves structured validation errors from trade submission', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({
      code: 'REQUEST_VALIDATION_FAILED',
      message: 'Trade request is invalid',
      fieldErrors: { currencyCode: 'Unsupported currency' },
      requestId: 'request-123'
    }), { status: 400, headers: { 'Content-Type': 'application/json' } })))

    await expect(submitTrade({ tradeReference: 'TRD-1' })).rejects.toMatchObject({
      name: 'ApiError',
      code: 'REQUEST_VALIDATION_FAILED',
      status: 400,
      fieldErrors: { currencyCode: 'Unsupported currency' },
      requestId: 'request-123'
    })
  })

  it('uses a useful fallback when an error response is not JSON', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('gateway unavailable', { status: 503 })))

    await expect(getFailedTrades()).rejects.toEqual(expect.objectContaining({
      constructor: ApiError,
      message: 'Failed trades lookup failed with HTTP 503',
      status: 503
    }))
  })
})
