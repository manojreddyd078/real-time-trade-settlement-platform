import { isAuthenticated } from '../services/auth.js'

export default function ProtectedRoute({ children, fallback = null }) {
  if (!isAuthenticated()) return fallback
  return children
}
