import { useState } from 'react'
import { submitTrade } from '../services/api.js'
import { TradeType } from '../types/trade.js'
import { validateTrade } from '../utils/tradeValidation.js'

const initialValues = {
  tradeReference: '', externalReference: '', tradeType: TradeType.BUY,
  instrumentId: '', buyerCounterpartyId: '', sellerCounterpartyId: '',
  quantity: '', price: '', currencyCode: 'USD', tradeDate: '', settlementDate: ''
}

function Field({ label, name, error, children, ...props }) {
  return <label className="form-field"><span>{label}</span>{children || <input name={name} aria-invalid={Boolean(error)} aria-describedby={error ? `${name}-error` : undefined} {...props} />}{error && <small id={`${name}-error`} className="field-error">{error}</small>}</label>
}

function TradeForm() {
  const [values, setValues] = useState(initialValues)
  const [errors, setErrors] = useState({})
  const [submission, setSubmission] = useState({ state: 'idle' })

  const update = (event) => {
    const { name, value } = event.target
    setValues((current) => ({ ...current, [name]: name === 'currencyCode' ? value.toUpperCase() : value }))
    setErrors((current) => ({ ...current, [name]: undefined }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    const nextErrors = validateTrade(values)
    if (Object.keys(nextErrors).length) { setErrors(nextErrors); setSubmission({ state: 'idle' }); return }
    setSubmission({ state: 'loading' })
    try {
      const result = await submitTrade({ ...values, quantity: Number(values.quantity), price: Number(values.price) })
      setSubmission({ state: 'success', result })
      setValues(initialValues)
    } catch (error) {
      setErrors(error.fieldErrors || {})
      setSubmission({ state: 'error', message: error.message, requestId: error.requestId })
    }
  }

  return (
    <form className="trade-form" onSubmit={handleSubmit} noValidate>
      {submission.state === 'success' && <div className="notice notice--success" role="status"><strong>Trade submitted successfully</strong><span>Generated trade ID</span><code>{submission.result.id}</code><small>Status: {submission.result.status}</small></div>}
      {submission.state === 'error' && <div className="notice notice--error" role="alert"><strong>Trade could not be submitted</strong><span>{submission.message}</span>{submission.requestId && <small>Request ID: {submission.requestId}</small>}</div>}
      <section className="form-section"><div className="form-section-title"><span>01</span><div><h3>Trade details</h3><p>Core identifiers and transaction side</p></div></div><div className="form-grid">
        <Field label="Trade reference" name="tradeReference" value={values.tradeReference} onChange={update} error={errors.tradeReference} placeholder="TRD-2026-10001" />
        <Field label="External reference" name="externalReference" value={values.externalReference} onChange={update} error={errors.externalReference} placeholder="OMS-88421 (optional)" />
        <Field label="Trade side" name="tradeType" error={errors.tradeType}><select name="tradeType" value={values.tradeType} onChange={update}><option value="BUY">Buy</option><option value="SELL">Sell</option></select></Field>
        <Field label="Instrument ID" name="instrumentId" value={values.instrumentId} onChange={update} error={errors.instrumentId} placeholder="UUID from reference data" />
      </div></section>
      <section className="form-section"><div className="form-section-title"><span>02</span><div><h3>Counterparties</h3><p>Buyer and seller reference identifiers</p></div></div><div className="form-grid">
        <Field label="Buyer counterparty ID" name="buyerCounterpartyId" value={values.buyerCounterpartyId} onChange={update} error={errors.buyerCounterpartyId} placeholder="Buyer UUID" />
        <Field label="Seller counterparty ID" name="sellerCounterpartyId" value={values.sellerCounterpartyId} onChange={update} error={errors.sellerCounterpartyId} placeholder="Seller UUID" />
      </div></section>
      <section className="form-section"><div className="form-section-title"><span>03</span><div><h3>Economics & settlement</h3><p>Quantity, price, currency, and value dates</p></div></div><div className="form-grid form-grid--three">
        <Field label="Quantity" name="quantity" type="number" min="0" step="0.0001" value={values.quantity} onChange={update} error={errors.quantity} placeholder="1000" />
        <Field label="Price" name="price" type="number" min="0" step="0.00000001" value={values.price} onChange={update} error={errors.price} placeholder="125.75" />
        <Field label="Currency" name="currencyCode" maxLength="3" value={values.currencyCode} onChange={update} error={errors.currencyCode} />
        <Field label="Trade date" name="tradeDate" type="date" value={values.tradeDate} onChange={update} error={errors.tradeDate} />
        <Field label="Settlement date" name="settlementDate" type="date" value={values.settlementDate} onChange={update} error={errors.settlementDate} />
      </div></section>
      <div className="form-actions"><button type="button" className="button-secondary" onClick={() => { setValues(initialValues); setErrors({}); setSubmission({ state: 'idle' }) }} disabled={submission.state === 'loading'}>Clear form</button><button type="submit" disabled={submission.state === 'loading'}>{submission.state === 'loading' ? <><span className="spinner" />Submitting…</> : 'Submit trade'}</button></div>
    </form>
  )
}

export default TradeForm
