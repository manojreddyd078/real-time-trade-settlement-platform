export const TradeStatus = Object.freeze({
  RECEIVED: 'RECEIVED', VALIDATED: 'VALIDATED', ENRICHED: 'ENRICHED',
  ELIGIBLE: 'ELIGIBLE', SETTLEMENT_PENDING: 'SETTLEMENT_PENDING',
  SETTLED: 'SETTLED', FAILED: 'FAILED', REJECTED: 'REJECTED'
})

export const TradeType = Object.freeze({ BUY: 'BUY', SELL: 'SELL' })

export const SettlementEligibilityStatus = Object.freeze({
  PENDING: 'PENDING', ELIGIBLE: 'ELIGIBLE', INELIGIBLE: 'INELIGIBLE'
})

/**
 * @typedef {Object} Trade
 * @property {string} id
 * @property {string} tradeReference
 * @property {keyof typeof TradeType} tradeType
 * @property {keyof typeof TradeStatus} status
 * @property {string} instrumentCode
 * @property {string} counterparty
 * @property {number} quantity
 * @property {number} price
 * @property {string} currencyCode
 * @property {string} settlementDate
 */

export const formatTradeStatus = (status) =>
  status ? status.toLowerCase().replaceAll('_', ' ').replace(/^\w/, (character) => character.toUpperCase()) : 'Unknown'

export const isExceptionStatus = (status) =>
  status === TradeStatus.FAILED || status === TradeStatus.REJECTED

export function formatTimestamp(value) {
  if (!value) return 'Not available'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? 'Not available' : date.toLocaleString()
}

export function formatDuration(start, end) {
  if (!start || !end) return 'Not available'
  const milliseconds = new Date(end).getTime() - new Date(start).getTime()
  if (!Number.isFinite(milliseconds) || milliseconds < 0) return 'Not available'
  if (milliseconds < 1000) return `${milliseconds} ms`
  const totalSeconds = Math.floor(milliseconds / 1000)
  if (totalSeconds < 60) return `${totalSeconds} sec`
  const totalMinutes = Math.floor(totalSeconds / 60)
  if (totalMinutes < 60) return `${totalMinutes} min ${totalSeconds % 60} sec`
  const totalHours = Math.floor(totalMinutes / 60)
  if (totalHours < 24) return `${totalHours} hr ${totalMinutes % 60} min`
  const days = Math.floor(totalHours / 24)
  return `${days} day${days === 1 ? '' : 's'} ${totalHours % 24} hr`
}
