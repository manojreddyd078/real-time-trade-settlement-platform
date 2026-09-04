import { useEffect, useState } from 'react'
import { getBackendStatus } from './services/api.js'

function App() {
  const [backend, setBackend] = useState({ state: 'checking', service: '' })

  useEffect(() => {
    getBackendStatus()
      .then((data) => setBackend({ state: data.status, service: data.service }))
      .catch(() => setBackend({ state: 'UNAVAILABLE', service: 'trade-settlement-backend' }))
  }, [])

  return (
    <main className="shell">
      <section className="card">
        <p className="eyebrow">Settlement operations</p>
        <h1>Real-Time Trade Settlement Platform</h1>
        <p className="description">
          The frontend is running and checking its connection to the Spring Boot API.
        </p>
        <div className="status" aria-live="polite">
          <span className={`indicator indicator--${backend.state.toLowerCase()}`} />
          <span>Backend: {backend.state}</span>
          {backend.service && <small>{backend.service}</small>}
        </div>
      </section>
    </main>
  )
}

export default App
