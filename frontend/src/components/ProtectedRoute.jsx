import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

export default function ProtectedRoute({ children }) {
  const { token, loading } = useAuth();
  if (loading) {
    return <div className="center muted" style={{ padding: '2rem' }}>Chargement…</div>;
  }
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return children;
}
