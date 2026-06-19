import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import NotificationBell from './NotificationBell.jsx';
import Avatar from './Avatar.jsx';

export default function AppShell() {
  const { user } = useAuth();
  const initiales = user ? (user.prenom?.[0] || '') + (user.nom?.[0] || '') : '';

  return (
    <div className="app">
      <header className="topbar">
        <span className="logo">Covoiturage</span>
        <nav className="topbar-nav">
          <NavLink to="/" end>
            Accueil
          </NavLink>
          <NavLink to="/vehicules">Véhicules</NavLink>
          <NavLink to="/historique">Trajets</NavLink>
        </nav>
        <div className="topbar-right">
          <NotificationBell />
          <NavLink to="/profil" className="topbar-user" title="Profil">
            <Avatar photoUrl={user?.photoUrl} initiales={initiales} size={36} />
          </NavLink>
        </div>
      </header>
      <div className="app-content">
        <Outlet />
      </div>
    </div>
  );
}
