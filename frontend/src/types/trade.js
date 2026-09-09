export const TradeStatus = Object.freeze({
  RECEIVED: 'RECEIVED', VALIDATING: 'VALIDATING', VALIDATED: 'VALIDATED',
  ENRICHED: 'ENRICHED', READY_FOR_SETTLEMENT: 'READY_FOR_SETTLEMENT',
  SETTLEMENT_PENDING: 'SETTLEMENT_PENDING', SETTLED: 'SETTLED',
  FAILED: 'FAILED', CANCELLED: 'CANCELLED'
})

export const TradeType = Object.freeze({ BUY: 'BUY', SELL: 'SELL' })

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
