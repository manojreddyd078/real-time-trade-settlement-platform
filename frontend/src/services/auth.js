const TOKEN_KEY = 'trade_settlement_access_token'

export function getAccessToken() { return sessionStorage.getItem(TOKEN_KEY) }
export function setAccessToken(token) { sessionStorage.setItem(TOKEN_KEY, token) }
export function clearSession() { sessionStorage.removeItem(TOKEN_KEY) }
export function isAuthenticated() { return Boolean(getAccessToken()) }

export function handleUnauthorized(response) {
  if (response.status === 401 || response.status === 403) {
    clearSession()
    window.dispatchEvent(new CustomEvent('auth:expired'))
  }
  return response
}
