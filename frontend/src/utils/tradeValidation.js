const uuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i
const referencePattern = /^[A-Za-z0-9][A-Za-z0-9._-]*$/

export function validateTrade(values) {
  const errors = {}
  if (!values.tradeReference.trim()) errors.tradeReference = 'Trade reference is required.'
  else if (values.tradeReference.length > 64 || !referencePattern.test(values.tradeReference)) errors.tradeReference = 'Use up to 64 letters, numbers, dots, underscores, or hyphens.'
  if (values.externalReference.length > 100) errors.externalReference = 'External reference must be 100 characters or fewer.'
  if (!values.tradeType) errors.tradeType = 'Trade side is required.'
  for (const field of ['instrumentId', 'buyerCounterpartyId', 'sellerCounterpartyId']) {
    if (!uuidPattern.test(values[field])) errors[field] = 'Enter a valid UUID.'
  }
  if (values.buyerCounterpartyId && values.buyerCounterpartyId === values.sellerCounterpartyId) errors.sellerCounterpartyId = 'Buyer and seller must be different.'
  if (!values.quantity || Number(values.quantity) <= 0) errors.quantity = 'Quantity must be greater than zero.'
  if (values.price === '' || Number(values.price) < 0) errors.price = 'Price cannot be negative.'
  if (!/^[A-Z]{3}$/.test(values.currencyCode)) errors.currencyCode = 'Enter a three-letter uppercase currency code.'
  if (!values.tradeDate) errors.tradeDate = 'Trade date is required.'
  if (!values.settlementDate) errors.settlementDate = 'Settlement date is required.'
  else if (values.tradeDate && values.settlementDate < values.tradeDate) errors.settlementDate = 'Settlement date cannot be before trade date.'
  return errors
}
