import { useEffect, useState } from 'react'
import { getReferenceData } from '../services/api.js'

export default function ReferenceDataPage() {
  const [state, setState] = useState({ loading: true, instruments: [], counterparties: [], error: null })
  const load = async () => { setState((s) => ({ ...s, loading: true, error: null })); try { const data = await getReferenceData(); setState({ loading: false, ...data, error: null }) } catch (error) { setState((s) => ({ ...s, loading: false, error: error.message })) } }
  useEffect(() => { load() }, [])
  return <section className="reference-page"><div className="intro"><div><p className="eyebrow">Reference data</p><h2>Instruments & counterparties</h2><p>Active records used to validate and enrich trades.</p></div><button type="button" onClick={load} disabled={state.loading}>{state.loading ? 'Loading…' : 'Refresh'}</button></div>{state.error && <div className="notice notice--error" role="alert">{state.error}</div>}{!state.loading && !state.error && <div className="reference-grid"><section className="panel"><div className="panel-heading"><h3>Instruments</h3><span>{state.instruments.length} records</span></div><ul className="reference-list">{state.instruments.map((item) => <li key={item.id}><strong>{item.instrumentCode}</strong><span>{item.name} · {item.currencyCode}</span><code>{item.id}</code></li>)}</ul></section><section className="panel"><div className="panel-heading"><h3>Counterparties</h3><span>{state.counterparties.length} records</span></div><ul className="reference-list">{state.counterparties.map((item) => <li key={item.id}><strong>{item.counterpartyCode}</strong><span>{item.legalName} · {item.countryCode}</span><code>{item.id}</code></li>)}</ul></section></div>}</section>
}
