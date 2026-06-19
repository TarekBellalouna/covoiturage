import { useAuth } from '../auth/AuthContext.jsx';
import PassengerHome from './PassengerHome.jsx';
import DriverDashboard from './DriverDashboard.jsx';

export default function Home() {
  const { user } = useAuth();
  if (!user) {
    return (
      <div className="page-center">
        <div className="card">
          <p className="muted">Chargement…</p>
        </div>
      </div>
    );
  }
  return user.modeActif === 'CONDUCTEUR' ? <DriverDashboard /> : <PassengerHome />;
}
