import MetricCard from '../components/MetricCard.jsx'

const initialMetrics = [
  { label: 'Trades today', value: '1,284', detail: '↑ 8.2% from yesterday', tone: 'positive' },
  { label: 'Trade volume', value: '$312.4M', detail: 'Across all instruments' },
  { label: 'Settled today', value: '851', detail: '66.3% completion', tone: 'positive' },
  { label: 'Failed / rejected', value: '7', detail: '3 require attention', tone: 'warning' }
]

export default function DashboardPage({ children }) {
  return <><section className="metrics" aria-label="Settlement metrics">{initialMetrics.map((metric) => <MetricCard key={metric.label} {...metric} />)}</section>{children}</>
}
