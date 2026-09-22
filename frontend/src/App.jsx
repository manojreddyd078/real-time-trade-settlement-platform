import { useEffect, useState } from 'react'
import MetricCard from './components/MetricCard.jsx'
import TradeTable from './components/TradeTable.jsx'
import TradeSubmissionPage from './pages/TradeSubmissionPage.jsx'
import TradeDetailsPage from './pages/TradeDetailsPage.jsx'
import { getBackendStatus } from './services/api.js'
import { TradeStatus, TradeType } from './types/trade.js'

const sampleTrades = [
  { id: '1', tradeReference: 'TRD-2026-09421', tradeType: TradeType.BUY, status: TradeStatus.READY_FOR_SETTLEMENT, instrumentCode: 'US91282CJL6', counterparty: 'Northstar Capital', quantity: 250000, price: 99.42, currencyCode: 'USD', settlementDate: 'Sep 11, 2026' },
  { id: '2', tradeReference: 'TRD-2026-09420', tradeType: TradeType.SELL, status: TradeStatus.SETTLEMENT_PENDING, instrumentCode: 'AAPL', counterparty: 'Meridian Securities', quantity: 12000, price: 228.16, currencyCode: 'USD', settlementDate: 'Sep 10, 2026' },
  { id: '3', tradeReference: 'TRD-2026-09419', tradeType: TradeType.BUY, status: TradeStatus.FAILED, instrumentCode: 'XS2196322150', counterparty: 'Apex Markets', quantity: 500000, price: 101.08, currencyCode: 'EUR', settlementDate: 'Sep 10, 2026' },
  { id: '4', tradeReference: 'TRD-2026-09418', tradeType: TradeType.SELL, status: TradeStatus.SETTLED, instrumentCode: 'MSFT', counterparty: 'Harbor Street Bank', quantity: 8500, price: 501.34, currencyCode: 'USD', settlementDate: 'Sep 09, 2026' }
]

function Overview({ onCapture, onSelectTrade }) {
  return <><section className="intro"><div><h2>Today’s settlement overview</h2><p>Monitor trade flow, settlement readiness, and exceptions.</p></div><button type="button" onClick={onCapture}>+ Capture trade</button></section><section className="metrics" aria-label="Settlement metrics"><MetricCard label="Trades today" value="1,284" detail="↑ 8.2% from yesterday" tone="positive" /><MetricCard label="Pending settlement" value="426" detail="$184.6M gross value" /><MetricCard label="Settled today" value="851" detail="66.3% completion" tone="positive" /><MetricCard label="Exceptions" value="7" detail="3 require attention" tone="warning" /></section><section className="panel" id="trades"><div className="panel-heading"><div><h2>Recent trades</h2><p>Latest activity across the settlement lifecycle</p></div><button className="link-button" type="button" onClick={onCapture}>Submit trade →</button></div><TradeTable trades={sampleTrades} onSelect={onSelectTrade} /></section></>
}

function App() {
  const [backend, setBackend] = useState({ state: 'checking', service: '' })
  const [page, setPage] = useState('overview')
  const [selectedTrade, setSelectedTrade] = useState(null)

  useEffect(() => {
    getBackendStatus().then((data) => setBackend({ state: data.status, service: data.service })).catch(() => setBackend({ state: 'UNAVAILABLE', service: 'trade-settlement-backend' }))
  }, [])

  return <div className="app-shell"><aside className="sidebar"><div className="brand"><span>TS</span><strong>Clearline</strong></div><nav aria-label="Main navigation"><button className={`nav-item ${page === 'overview' ? 'nav-item--active' : ''}`} onClick={() => setPage('overview')}>Overview</button><button className={`nav-item ${page === 'submit' ? 'nav-item--active' : ''}`} onClick={() => setPage('submit')}>Submit trade</button><button className="nav-item">Settlements</button><button className="nav-item">Exceptions <span className="count">3</span></button><button className="nav-item">Reference data</button></nav><div className="sidebar-footer"><span className={`indicator indicator--${backend.state.toLowerCase()}`} /><div><strong>System {backend.state}</strong><small>{backend.service || 'Connecting…'}</small></div></div></aside><main className="content"><header className="topbar"><div><p className="eyebrow">Settlement operations</p><h1>{page === 'submit' ? 'Trade capture' : page === 'details' ? 'Trade details' : 'Good morning, Alex'}</h1></div><div className="operator"><span>AR</span><div><strong>Alex Rivera</strong><small>Operations analyst</small></div></div></header>{page === 'submit' ? <TradeSubmissionPage onBack={() => setPage('overview')} /> : page === 'details' ? <TradeDetailsPage tradeId={selectedTrade.id} fallback={selectedTrade} onBack={() => setPage('overview')} /> : <Overview onCapture={() => setPage('submit')} onSelectTrade={(trade) => { setSelectedTrade(trade); setPage('details') }} />}</main></div>
}

export default App
