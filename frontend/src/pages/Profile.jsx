import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import api from '../api/client.js';
import Avatar from '../components/Avatar.jsx';

function lireImage(file, max = 400) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const img = new Image();
      img.onload = () => {
        const echelle = Math.min(1, max / Math.max(img.width, img.height));
        const w = Math.round(img.width * echelle);
        const h = Math.round(img.height * echelle);
        const canvas = document.createElement('canvas');
        canvas.width = w;
        canvas.height = h;
        canvas.getContext('2d').drawImage(img, 0, 0, w, h);
        resolve(canvas.toDataURL('image/jpeg', 0.8));
      };
      img.onerror = reject;
      img.src = reader.result;
    };
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

export default function Profile() {
  const { user, logout, refreshUser } = useAuth();
  const [reputation, setReputation] = useState(null);
  const [edition, setEdition] = useState(false);
  const [form, setForm] = useState({ nom: '', prenom: '', telephone: '', photoUrl: '' });
  const [erreur, setErreur] = useState('');
  const [enregistre, setEnregistre] = useState(false);
  const fichierRef = useRef(null);

  useEffect(() => {
    if (!user) return;
    setForm({
      nom: user.nom || '',
      prenom: user.prenom || '',
      telephone: user.telephone || '',
      photoUrl: user.photoUrl || '',
    });
    api
      .get(`/api/users/${user.id}/reputation`)
      .then(({ data }) => setReputation(data))
      .catch(() => {});
  }, [user]);

  if (!user) {
    return (
      <div className="page-center">
        <div className="card">
          <p className="muted">Chargement…</p>
        </div>
      </div>
    );
  }

  const set = (champ) => (e) => setForm({ ...form, [champ]: e.target.value });

  const choisirPhoto = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      const dataUrl = await lireImage(file);
      setForm((f) => ({ ...f, photoUrl: dataUrl }));
    } catch {
      setErreur('Image illisible');
    }
  };

  const enregistrer = async () => {
    setErreur('');
    setEnregistre(false);
    try {
      await api.patch('/api/users/me/profil', form);
      await refreshUser();
      setEnregistre(true);
      setEdition(false);
    } catch {
      setErreur('Enregistrement impossible');
    }
  };

  const changerRole = async () => {
    setErreur('');
    const cible = user.modeActif === 'CONDUCTEUR' ? 'PASSAGER' : 'CONDUCTEUR';
    try {
      await api.patch('/api/users/me/mode', { mode: cible });
      await refreshUser();
    } catch {
      setErreur('Changement de rôle impossible');
    }
  };

  const initiales = (user.prenom?.[0] || '') + (user.nom?.[0] || '');
  const moyenne = reputation && reputation.nombreAvis > 0 ? reputation.noteMoyenne.toFixed(1) : '—';

  return (
    <div className="page-center">
      <div className="card">
        <div className="home-greeting" style={{ marginBottom: 16 }}>
          <Avatar
            photoUrl={edition ? form.photoUrl : user.photoUrl}
            initiales={initiales}
            size={64}
          />
          <div>
            <h2 style={{ margin: 0 }}>
              {user.prenom} {user.nom}
            </h2>
            <span className="muted">{user.email}</span>
          </div>
        </div>

        {!edition ? (
          <>
            <div className="info-row">
              <span className="muted">Téléphone</span>
              <strong>{user.telephone || '—'}</strong>
            </div>
            <div className="info-row">
              <span className="muted">Rôle actif</span>
              <strong>{user.modeActif === 'CONDUCTEUR' ? 'Conducteur' : 'Passager'}</strong>
            </div>
            <div className="info-row">
              <span className="muted">Réputation</span>
              <strong>
                ★ {moyenne}
                {reputation ? ` (${reputation.nombreAvis} avis)` : ''}
              </strong>
            </div>
            {enregistre && (
              <p className="muted" style={{ color: '#3b6d11' }}>
                Profil mis à jour.
              </p>
            )}
            <div style={{ display: 'flex', gap: 12, marginTop: 12, flexWrap: 'wrap' }}>
              <button className="btn btn-primary" onClick={() => setEdition(true)}>
                Modifier le profil
              </button>
              <button className="btn" onClick={changerRole}>
                {user.modeActif === 'CONDUCTEUR' ? 'Passer en passager' : 'Passer en conducteur'}
              </button>
              <Link className="btn" to="/vehicules" style={{ textAlign: 'center' }}>
                Mes véhicules
              </Link>
              <button className="btn btn-danger" onClick={logout}>
                Se déconnecter
              </button>
            </div>
          </>
        ) : (
          <>
            <div className="photo-edit">
              <Avatar photoUrl={form.photoUrl} initiales={initiales} size={72} />
              <div>
                <button className="btn" onClick={() => fichierRef.current?.click()}>
                  Changer la photo
                </button>
                {form.photoUrl && (
                  <button
                    className="link-danger"
                    style={{ marginLeft: 12 }}
                    onClick={() => setForm({ ...form, photoUrl: '' })}
                  >
                    Retirer
                  </button>
                )}
                <input
                  ref={fichierRef}
                  type="file"
                  accept="image/*"
                  hidden
                  onChange={choisirPhoto}
                />
              </div>
            </div>

            <div className="grid-2">
              <div>
                <label className="label">Prénom</label>
                <input className="field" value={form.prenom} onChange={set('prenom')} />
              </div>
              <div>
                <label className="label">Nom</label>
                <input className="field" value={form.nom} onChange={set('nom')} />
              </div>
            </div>
            <label className="label">Téléphone</label>
            <input className="field" value={form.telephone} onChange={set('telephone')} />

            {erreur && <p className="error">{erreur}</p>}
            <div style={{ display: 'flex', gap: 12, marginTop: 14 }}>
              <button className="btn btn-primary" onClick={enregistrer}>
                Enregistrer
              </button>
              <button className="btn" onClick={() => setEdition(false)}>
                Annuler
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
