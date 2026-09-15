import TradeForm from '../components/TradeForm.jsx'

function TradeSubmissionPage({ onBack }) {
  return <section className="submission-page"><button className="back-link" type="button" onClick={onBack}>← Back to overview</button><div className="page-heading"><p className="eyebrow">Trade capture</p><h2>Submit a new trade</h2><p>Enter trade details below. All reference IDs must exist and be active.</p></div><TradeForm /></section>
}

export default TradeSubmissionPage
