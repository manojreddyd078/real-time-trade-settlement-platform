function MetricCard({ label, value, detail, tone = 'neutral' }) {
  return (
    <article className={`metric metric--${tone}`}>
      <p>{label}</p><strong>{value}</strong><span>{detail}</span>
    </article>
  )
}

export default MetricCard
