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
  status.toLowerCase().replaceAll('_', ' ').replace(/^\w/, (character) => character.toUpperCase())
