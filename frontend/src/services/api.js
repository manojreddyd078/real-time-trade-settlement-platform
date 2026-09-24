export async function getBackendStatus() {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || ''
  const response = await fetch(`${apiBaseUrl}/api/status`)

  if (!response.ok) {
    throw new Error(`Backend returned HTTP ${response.status}`)
  }

  return response.json()
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
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || ''
  const response = await fetch(`${apiBaseUrl}/api/trades`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(trade)
  })
  const body = await response.json().catch(() => null)

  if (!response.ok) {
    throw new ApiError(body?.message || `Trade submission failed with HTTP ${response.status}`, {
      code: body?.code,
      fieldErrors: body?.fieldErrors,
      requestId: body?.requestId,
      status: response.status
    })
  }

  return body
}

export async function getTrade(tradeId) {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || ''
  const response = await fetch(`${apiBaseUrl}/api/trades/${tradeId}`)
  const body = await response.json().catch(() => null)

  if (!response.ok) {
    throw new ApiError(body?.message || `Trade status lookup failed with HTTP ${response.status}`, {
      code: body?.code, requestId: body?.requestId, status: response.status
    })
  }

  return body
}

export async function getTradeDetails(tradeId) {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || ''
  const response = await fetch(`${apiBaseUrl}/api/trades/${tradeId}/details`)
  const body = await response.json().catch(() => null)
  if (!response.ok) throw new ApiError(body?.message || `Trade details lookup failed with HTTP ${response.status}`, { code: body?.code, requestId: body?.requestId, status: response.status })
  return body
}

export async function getSettlements() {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || ''
  const response = await fetch(`${apiBaseUrl}/api/settlements`)
  const body = await response.json().catch(() => null)
  if (!response.ok) throw new ApiError(body?.message || `Settlement status lookup failed with HTTP ${response.status}`, { code: body?.code, requestId: body?.requestId, status: response.status })
  return body
}
