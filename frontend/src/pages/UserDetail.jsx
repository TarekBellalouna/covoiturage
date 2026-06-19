import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/client.js';
import Avatar from '../components/Avatar.jsx';

export default function UserDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [profil, setProfil] = useState(null);
  const [erreur, setErreur] = useState('');

  useEffect(() => {
    api
      .get(`/api/users/${id}`)
      .then(({ data }) => setProfil(data))
      .catch(() => setErreur('Profil introuvable'));
  }, [id]);

  if (erreur) {
    return (
      <div className="page-center">
        <div className="card">
          <p className="error">{erreur}</p>
        </div>
      </div>
    );
  }
  if (!profil) {
    return (
      <div className="page-center">
        <div className="card">
          <p className="muted">Chargement…</p>
        </div>
      </div>
    );
  }

  const initiales = (profil.prenom?.[0] || '') + (profil.nom?.[0] || '');
  const moyenne = profil.nombreAvis > 0 ? profil.noteMoyenne.toFixed(1) : '—';

  return (
    <div className="page-center">
      <div className="card">
        <div className="home-greeting" style={{ marginBottom: 18 }}>
          <Avatar photoUrl={profil.photoUrl} initiales={initiales} size={72} />
          <div>
            <h2 style={{ margin: 0 }}>
              {profil.prenom} {profil.nom}
            </h2>
            <span className="muted">{profil.estConducteur ? 'Conducteur' : 'Passager'}</span>
          </div>
        </div>

        <div className="stats">
          <div className="stat">
            <span className="stat-num">★ {moyenne}</span>
            <span className="stat-lbl">{profil.nombreAvis} avis</span>
          </div>
          <div className="stat">
            <span className="stat-num">{profil.nombreTrajetsConducteur}</span>
            <span className="stat-lbl">Trajets conduits</span>
          </div>
          <div className="stat">
            <span className="stat-num">{profil.nombreTrajetsPassager}</span>
            <span className="stat-lbl">Trajets passager</span>
          </div>
        </div>

        <div className="info-row">
          <span className="muted">Téléphone</span>
          <strong>{profil.telephone || '—'}</strong>
        </div>

        <button className="link" style={{ marginTop: 14 }} onClick={() => navigate(-1)}>
          ← Retour
        </button>
      </div>
    </div>
  );
}
