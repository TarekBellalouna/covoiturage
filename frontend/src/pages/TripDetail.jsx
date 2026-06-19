import { useCallback, useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/client.js';
import { useAuth } from '../auth/AuthContext.jsx';
import RatingModal from '../components/RatingModal.jsx';

const LABELS = {
  OUVERT: 'Ouvert',
  EN_COURS: 'En cours',
  TERMINE: 'Terminé',
  ANNULE: 'Annulé',
};

function Etoiles({ note }) {
  return (
    <span className="stars-read" aria-label={`${note} sur 5`}>
      {[1, 2, 3, 4, 5].map((n) => (
        <span key={n} className={n <= note ? 'star-on' : 'star-off'}>
          ★
        </span>
      ))}
    </span>
  );
}

export default function TripDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const [trajet, setTrajet] = useState(null);
  const [erreur, setErreur] = useState('');
  const [aNoter, setANoter] = useState(null);

  const charger = useCallback(() => {
    api
      .get(`/api/trajets/${id}/details`)
      .then(({ data }) => setTrajet(data))
      .catch(() => setErreur('Trajet introuvable ou accès refusé'));
  }, [id]);

  useEffect(() => {
    charger();
  }, [charger]);

  if (erreur) {
    return (
      <div className="page-center">
        <div className="card">
          <p className="error">{erreur}</p>
          <Link className="link" to="/historique">
            ← Retour à l’historique
          </Link>
        </div>
      </div>
    );
  }

  if (!trajet) {
    return (
      <div className="page-center">
        <div className="card">
          <p className="muted">Chargement…</p>
        </div>
      </div>
    );
  }

  const estConducteur = user && trajet.conducteur.id === user.id;
  const termine = trajet.statut === 'TERMINE';

  return (
    <div className="page-center">
      <div className="card">
        <div className="row-between">
          <h2 style={{ margin: 0 }}>Détail du trajet</h2>
          <span className={`badge badge-${trajet.statut}`}>{LABELS[trajet.statut] || trajet.statut}</span>
        </div>
        <p className="muted" style={{ marginTop: 4 }}>
          Départ : {trajet.pointDepart.nom}
        </p>
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <h3 className="detail-h3">Conducteur</h3>
        <div className="info-row">
          <Link className="link" to={`/users/${trajet.conducteur.id}`}>
            {trajet.conducteur.prenom} {trajet.conducteur.nom}
          </Link>
          <span className="muted">{trajet.conducteur.telephone || '—'}</span>
        </div>
        {!estConducteur && termine && (
          <button className="btn btn-primary" onClick={() => setANoter({ cibleId: null, titre: 'Noter le conducteur' })}>
            Noter le conducteur
          </button>
        )}
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <h3 className="detail-h3">Véhicule</h3>
        <div className="info-row">
          <span>
            {trajet.vehicule.marque} {trajet.vehicule.modele}
          </span>
          <span className="muted">
            {trajet.vehicule.couleur || '—'} · {trajet.vehicule.immatriculation || '—'} ·{' '}
            {trajet.vehicule.nombrePlaces} places
          </span>
        </div>
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <h3 className="detail-h3">Passagers</h3>
        {trajet.passagers.length === 0 && <p className="muted">Aucun passager.</p>}
        {trajet.passagers.map((p) => (
          <div key={p.id} className="info-row">
            <span>
              <Link className="link" to={`/users/${p.id}`}>
                {p.prenom} {p.nom}
              </Link>{' '}
              → {p.destinationTexte} ({p.nombrePlaces} place(s))
            </span>
            {estConducteur && termine && (
              <button
                className="link"
                onClick={() => setANoter({ cibleId: p.id, titre: `Noter ${p.prenom}` })}
              >
                Noter
              </button>
            )}
          </div>
        ))}
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <h3 className="detail-h3">Évaluations</h3>
        {trajet.evaluations.length === 0 && <p className="muted">Aucune évaluation pour l’instant.</p>}
        {trajet.evaluations.map((e, i) => (
          <div key={i} className="eval-item">
            <div className="row-between">
              <strong>
                {e.auteurPrenom} → {e.ciblePrenom}
              </strong>
              <Etoiles note={e.note} />
            </div>
            {e.commentaire && <p className="muted" style={{ margin: '4px 0 0' }}>{e.commentaire}</p>}
          </div>
        ))}
      </div>

      {aNoter && (
        <RatingModal
          trajetId={trajet.id}
          titre={aNoter.titre}
          cibleId={aNoter.cibleId}
          onClose={() => {
            setANoter(null);
            charger();
          }}
        />
      )}
    </div>
  );
}
