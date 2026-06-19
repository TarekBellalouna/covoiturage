import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotifications } from '../ws/NotificationProvider.jsx';

const LIBELLE = {
  DEMANDE_RECUE: 'Nouvelle demande',
  DEMANDE_ACCEPTEE: 'Demande acceptée',
  TRAJET_DEMARRE: 'Trajet démarré',
  TRAJET_TERMINE: 'Trajet terminé',
  TRAJET_ANNULE: 'Trajet annulé',
};

function tempsRelatif(iso) {
  const diff = (Date.now() - new Date(iso).getTime()) / 1000;
  if (diff < 60) return "à l'instant";
  if (diff < 3600) return `il y a ${Math.floor(diff / 60)} min`;
  if (diff < 86400) return `il y a ${Math.floor(diff / 3600)} h`;
  return `il y a ${Math.floor(diff / 86400)} j`;
}

export default function NotificationBell() {
  const { items, unread, marquerToutLu } = useNotifications();
  const [ouvert, setOuvert] = useState(false);
  const navigate = useNavigate();

  const basculer = () => {
    const prochain = !ouvert;
    setOuvert(prochain);
    if (prochain && unread > 0) marquerToutLu();
  };

  const ouvrir = (n) => {
    setOuvert(false);
    if (n.trajetId) navigate(`/trajets/${n.trajetId}`);
  };

  return (
    <div className="bell-wrap">
      <button className="bell" onClick={basculer} aria-label="Notifications">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
          strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
        {unread > 0 && <span className="bell-badge">{unread > 9 ? '9+' : unread}</span>}
      </button>

      {ouvert && (
        <>
          <div className="dropdown-backdrop" onClick={() => setOuvert(false)} />
          <div className="dropdown">
            <div className="dropdown-head">Notifications</div>
            {items.length === 0 && <div className="dropdown-empty muted">Aucune notification</div>}
            {items.map((n) => (
              <button
                key={n.id}
                className={`notif-item ${n.lu ? '' : 'notif-unread'}`}
                onClick={() => ouvrir(n)}
              >
                <span className="notif-title">{LIBELLE[n.type] || n.type}</span>
                <span className="notif-msg">{n.message}</span>
                <span className="notif-time">{tempsRelatif(n.dateCreation)}</span>
              </button>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
