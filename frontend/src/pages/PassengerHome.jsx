import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/client.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { useStomp } from '../ws/StompProvider.jsx';
import MapView from '../components/MapView.jsx';
import RatingModal from '../components/RatingModal.jsx';
import Avatar from '../components/Avatar.jsx';
import DestinationAutocomplete from '../components/DestinationAutocomplete.jsx';

function couleur(statut) {
  switch (statut) {
    case 'EN_ATTENTE':
      return '#e08a1e'; // demande en attente
    case 'OUVERT':
      return '#0f6e56'; // trajet ouvert
    case 'EN_COURS':
      return '#2563a8'; // trajet en cours
    default:
      return '#6b6a64';
  }
}

export default function PassengerHome() {
  const { user } = useAuth();
  const { connected, subscribe } = useStomp();

  const [points, setPoints] = useState([]);
  const [pointId, setPointId] = useState('');
  const [destination, setDestination] = useState(null);
  const [places, setPlaces] = useState(1);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState('');

  const [demandeActive, setDemandeActive] = useState(null);
  const [trajetActif, setTrajetActif] = useState(null);
  const [reputation, setReputation] = useState(null);
  const [nbDemandes, setNbDemandes] = useState(0);
  const [nbTrajets, setNbTrajets] = useState(0);
  const [activite, setActivite] = useState([]);
  const [noter, setNoter] = useState(false);

  useEffect(() => {
    api
      .get('/api/points-rencontre')
      .then(({ data }) => {
        setPoints(data);
        if (data.length) setPointId(String(data[0].id));
      })
      .catch(() => {});
  }, []);

  const chargerMonActivite = useCallback(() => {
    api
      .get('/api/demandes/me')
      .then(({ data }) => {
        setNbDemandes(data.length);
        setDemandeActive(
          data.find((d) => ['EN_ATTENTE', 'ACCEPTEE', 'EN_COURS'].includes(d.statut)) || null
        );
      })
      .catch(() => {});
    api
      .get('/api/trajets/me')
      .then(({ data }) => {
        setNbTrajets(data.length);
        setTrajetActif(data.find((t) => t.statut === 'OUVERT' || t.statut === 'EN_COURS') || null);
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    chargerMonActivite();
  }, [chargerMonActivite]);

  useEffect(() => {
    if (!user) return;
    api
      .get(`/api/users/${user.id}/reputation`)
      .then(({ data }) => setReputation(data))
      .catch(() => {});
  }, [user]);

  const chargerCarte = useCallback(() => {
    api
      .get('/api/carte/activite')
      .then(({ data }) => setActivite(data))
      .catch(() => {});
  }, []);

  useEffect(() => {
    chargerCarte();
    const id = setInterval(chargerCarte, 20000);
    return () => clearInterval(id);
  }, [chargerCarte]);

  useEffect(() => {
    if (!connected || !demandeActive) return;
    const unsub = subscribe('/user/queue/suivi', (maj) => {
      if (maj.id !== demandeActive.id) return;
      setDemandeActive(maj);
      if (maj.statut === 'TERMINEE') setNoter(true);
    });
    return unsub;
  }, [connected, demandeActive, subscribe]);

  const demander = async () => {
    setErreur('');
    setEnCours(true);
    try {
      const { data } = await api.post('/api/demandes', {
        pointRencontreId: Number(pointId),
        destinationTexte: destination.label,
        destinationLat: destination.lat,
        destinationLng: destination.lng,
        nombrePlacesDemandees: places,
      });
      setDemandeActive(data);
      setDestination(null);
      chargerCarte();
    } catch {
      setErreur('La demande a échoué');
    } finally {
      setEnCours(false);
    }
  };

  const annuler = async () => {
    if (!demandeActive) return;
    try {
      await api.post(`/api/demandes/${demandeActive.id}/annuler`);
    } catch {
      /* ignore */
    }
    setDemandeActive(null);
    chargerMonActivite();
    chargerCarte();
  };

  const markers = activite.map((a, i) => ({
    id: `${a.type}-${i}`,
    lat: a.latitude,
    lng: a.longitude,
    label: a.label,
    color: couleur(a.statut),
  }));

  const note = reputation && reputation.nombreAvis > 0 ? reputation.noteMoyenne.toFixed(1) : '—';
  const initiales = user ? (user.prenom?.[0] || '') + (user.nom?.[0] || '') : '';
  const demandeEnAttente = demandeActive && demandeActive.statut === 'EN_ATTENTE';
  const demandeEnRoute =
    demandeActive && (demandeActive.statut === 'ACCEPTEE' || demandeActive.statut === 'EN_COURS');

  const route =
    demandeActive?.destinationLat != null
      ? [
          [demandeActive.latitude, demandeActive.longitude],
          [demandeActive.destinationLat, demandeActive.destinationLng],
        ]
      : null;

  const pointChoisi = points.find((p) => String(p.id) === pointId);
  const centre = demandeActive
    ? [demandeActive.latitude, demandeActive.longitude]
    : pointChoisi
      ? [pointChoisi.latitude, pointChoisi.longitude]
      : undefined;

  return (
    <div className="home">
      <section className="home-panel">
        <div className="home-greeting">
          <Avatar photoUrl={user?.photoUrl} initiales={initiales} size={48} />
          <div>
            <h2 style={{ margin: 0 }}>Bonjour {user?.prenom}</h2>
            <span className="muted">Passager</span>
          </div>
        </div>

        <div className="stats">
          <div className="stat">
            <span className="stat-num">{nbDemandes}</span>
            <span className="stat-lbl">Demandes</span>
          </div>
          <div className="stat">
            <span className="stat-num">{nbTrajets}</span>
            <span className="stat-lbl">Trajets conduits</span>
          </div>
          <div className="stat">
            <span className="stat-num">★ {note}</span>
            <span className="stat-lbl">
              {reputation ? `${reputation.nombreAvis} avis` : 'Note'}
            </span>
          </div>
        </div>

        {trajetActif && (
          <div className="card live-card">
            <div className="row-between">
              <strong>Votre trajet (conducteur)</strong>
              <span className={`badge badge-${trajetActif.statut}`}>{trajetActif.statut}</span>
            </div>
            <p className="muted">
              {trajetActif.nombrePassagers} passager(s) · départ {trajetActif.pointDepartNom}
            </p>
            <div style={{ display: 'flex', gap: 16 }}>
              <Link className="link" to={`/trajets/${trajetActif.id}`}>
                Voir le détail
              </Link>
              <Link className="link" to="/conducteur">
                Gérer
              </Link>
            </div>
          </div>
        )}

        {demandeEnRoute && (
          <div className="card live-card">
            <div className="row-between">
              <strong>Votre trajet (passager)</strong>
              <span className={`badge badge-${demandeActive.statut}`}>{demandeActive.statut}</span>
            </div>
            <p className="muted">
              {demandeActive.pointRencontreNom} → {demandeActive.destinationTexte}
            </p>
            {demandeActive.prix != null && (
              <p style={{ margin: '4px 0 0', fontWeight: 500 }}>Prix : {demandeActive.prix} €</p>
            )}
            <div style={{ display: 'flex', gap: 16 }}>
              {demandeActive.trajetId && (
                <Link className="link" to={`/trajets/${demandeActive.trajetId}`}>
                  Voir le détail
                </Link>
              )}
              <button className="link-danger" onClick={annuler}>
                Annuler
              </button>
            </div>
          </div>
        )}

        {!demandeEnRoute && (
          <div className="card">
            <h3 className="detail-h3">Demander un trajet</h3>
            {demandeEnAttente ? (
              <>
                <span className="badge badge-EN_ATTENTE">EN_ATTENTE</span>
                <p className="muted">Recherche d'un conducteur à proximité…</p>
                <button className="btn btn-danger" onClick={annuler}>
                  Annuler la demande
                </button>
              </>
            ) : (
              <>
                <label className="label">Point de rencontre</label>
                <select
                  className="field"
                  value={pointId}
                  onChange={(e) => setPointId(e.target.value)}
                >
                  {points.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.nom} — {p.ville}
                    </option>
                  ))}
                </select>
                <label className="label">Destination</label>
                <DestinationAutocomplete onSelect={setDestination} />
                <label className="label">Places</label>
                <div className="chips">
                  {[1, 2, 3].map((n) => (
                    <button
                      key={n}
                      type="button"
                      className={`chip ${places === n ? 'chip-active' : ''}`}
                      onClick={() => setPlaces(n)}
                    >
                      {n}
                    </button>
                  ))}
                </div>
                {erreur && <p className="error">{erreur}</p>}
                <button
                  className="btn btn-primary"
                  onClick={demander}
                  disabled={enCours || !destination?.lat || !pointId}
                >
                  {enCours ? 'Envoi…' : 'Demander un trajet'}
                </button>
              </>
            )}
          </div>
        )}
      </section>

      <div className="home-map">
        <MapView markers={markers} route={route} center={centre} />
        <div className="map-legend">
          <span>
            <i style={{ background: '#e08a1e' }} /> Demande en attente
          </span>
          <span>
            <i style={{ background: '#0f6e56' }} /> Trajet ouvert
          </span>
          <span>
            <i style={{ background: '#2563a8' }} /> Trajet en cours
          </span>
        </div>
      </div>

      {noter && demandeActive?.trajetId && (
        <RatingModal
          trajetId={demandeActive.trajetId}
          titre="Noter le conducteur"
          onClose={() => {
            setNoter(false);
            setDemandeActive(null);
            chargerMonActivite();
          }}
        />
      )}
    </div>
  );
}
