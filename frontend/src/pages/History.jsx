import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client.js';

const LABELS = {
  EN_ATTENTE: 'En attente',
  ACCEPTEE: 'Acceptée',
  EN_COURS: 'En cours',
  TERMINEE: 'Terminée',
  TERMINE: 'Terminé',
  OUVERT: 'Ouvert',
  EXPIREE: 'Expirée',
  ANNULEE: 'Annulée',
  ANNULE: 'Annulé',
};

export default function History() {
  const navigate = useNavigate();
  const [demandes, setDemandes] = useState([]);
  const [trajets, setTrajets] = useState([]);

  useEffect(() => {
    api.get('/api/demandes/me').then(({ data }) => setDemandes(data)).catch(() => {});
    api.get('/api/trajets/me').then(({ data }) => setTrajets(data)).catch(() => {});
  }, []);

  const ouvrir = (trajetId) => {
    if (trajetId) navigate(`/trajets/${trajetId}`);
  };

  return (
    <div className="page-center page-wide">
      <div className="history-grid">
        <div className="card">
          <h2>En tant que passager</h2>
          {demandes.length === 0 && <p className="muted">Aucune demande pour l’instant.</p>}
          {demandes.map((d) => (
            <div
              key={d.id}
              className={`info-row ${d.trajetId ? 'row-clickable' : ''}`}
              onClick={() => ouvrir(d.trajetId)}
            >
              <span>
                {d.pointRencontreNom} → {d.destinationTexte}
              </span>
              <span className={`badge badge-${d.statut}`}>{LABELS[d.statut] || d.statut}</span>
            </div>
          ))}
        </div>

        <div className="card">
          <h2>En tant que conducteur</h2>
          {trajets.length === 0 && <p className="muted">Aucun trajet conduit pour l’instant.</p>}
          {trajets.map((t) => (
            <div key={t.id} className="info-row row-clickable" onClick={() => ouvrir(t.id)}>
              <span>
                {t.pointDepartNom} · {t.nombrePassagers} passager(s)
              </span>
              <span className={`badge badge-${t.statut}`}>{LABELS[t.statut] || t.statut}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
