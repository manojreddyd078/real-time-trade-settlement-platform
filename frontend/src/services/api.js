import { getAccessToken, handleUnauthorized } from './auth.js'

async function request(path, options = {}) {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || ''
  const token = getAccessToken()
  const headers = { Accept: 'application/json', ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...(options.headers || {}) }
  if (token) headers.Authorization = `Bearer ${token}`
  let response
  try {
    response = await fetch(`${apiBaseUrl}${path}`, { ...options, headers })
  } catch (error) {
    throw new ApiError('Backend service could not be reached. Start Spring Boot on http://localhost:8080 and verify the frontend API URL.', { cause: error })
  }
  handleUnauthorized(response)
  const body = await response.json().catch(() => null)
  return { response, body }
}

function apiMessage(fallback, response, body) {
  if (response.status === 502 || response.status === 503 || response.status === 504) {
    return 'Backend service is unavailable. Check that Spring Boot, the database, and Kafka are running, then retry.'
  }
  return body?.message || `${fallback} (HTTP ${response.status})`
}

export async function getBackendStatus() {
  const { response, body } = await request('/api/status')

  if (!response.ok) {
    throw new Error(`Backend returned HTTP ${response.status}`)
  }

  return body
}

export class ApiError extends Error {
  constructor(message, { code, fieldErrors, requestId, status } = {}) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.fieldErrors = fieldErrors || {}
    this.requestId = requestId
    this.status = status
  }
}

export async function submitTrade(trade) {
  const { response, body } = await request('/api/trades', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(trade)
  })
  if (!response.ok) {
    throw new ApiError(apiMessage('Trade submission failed', response, body), {
      code: body?.code,
      fieldErrors: body?.fieldErrors,
      requestId: body?.requestId,
      status: response.status
    })
  }

  return body
}

export async function updateTrade(tradeId, trade) {
  const { response, body } = await request(`/api/trades/${encodeURIComponent(tradeId)}`, {
    method: 'PUT',
    body: JSON.stringify(trade)
  })
  if (!response.ok) throw new ApiError(apiMessage('Trade update failed', response, body), { code: body?.code, fieldErrors: body?.fieldErrors, requestId: body?.requestId, status: response.status })
  return body
}

export async function getTrade(tradeId) {
  const { response, body } = await request(`/api/trades/${encodeURIComponent(tradeId)}`)

  if (!response.ok) {
    throw new ApiError(body?.message || `Trade status lookup failed with HTTP ${response.status}`, {
      code: body?.code, requestId: body?.requestId, status: response.status
    })
  }

  return body
}

export async function getTradeDetails(tradeId) {
  const { response, body } = await request(`/api/trades/${encodeURIComponent(tradeId)}/details`)
  if (!response.ok) throw new ApiError(body?.message || `Trade details lookup failed with HTTP ${response.status}`, { code: body?.code, requestId: body?.requestId, status: response.status })
  return body
}

export async function getTradeStatusHistory(tradeId) {
  const { response, body } = await request(`/api/trades/${encodeURIComponent(tradeId)}/status`)
  if (!response.ok) throw new ApiError(body?.message || `Trade status history lookup failed with HTTP ${response.status}`, { code: body?.code, requestId: body?.requestId, status: response.status })
  return body
}

export async function getSettlements() {
  const { response, body } = await request('/api/settlements')
  if (!response.ok) throw new ApiError(apiMessage('Settlement status lookup failed', response, body), { code: body?.code, requestId: body?.requestId, status: response.status })
  return body
}

export async function getFailedTrades() {
  const { response, body } = await request('/api/failed-trades')
  if (!response.ok) throw new ApiError(apiMessage('Failed trades lookup failed', response, body), { code: body?.code, requestId: body?.requestId, status: response.status })
  return body
}

export async function getReferenceData() {
  const [instruments, counterparties] = await Promise.all([request('/api/reference-data/instruments'), request('/api/reference-data/counterparties')])
  for (const result of [instruments, counterparties]) if (!result.response.ok) throw new ApiError(apiMessage('Reference data lookup failed', result.response, result.body), { status: result.response.status })
  return { instruments: instruments.body || [], counterparties: counterparties.body || [] }
}

export async function getTransactionTrace(tradeId) {
  const { response, body } = await request(`/api/traces/${encodeURIComponent(tradeId)}`)
  if (!response.ok) throw new ApiError(apiMessage('Transaction trace lookup failed', response, body), { code: body?.code, requestId: body?.requestId, status: response.status })
  return body
}
