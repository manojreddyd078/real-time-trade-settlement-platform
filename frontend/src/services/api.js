export async function getBackendStatus() {
  const response = await fetch('/api/status')

  if (!response.ok) {
    throw new Error(`Backend returned HTTP ${response.status}`)
  }

  return response.json()
}
