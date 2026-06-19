import { useEffect, useState } from 'react';
import api from '../api/client.js';
import { useAuth } from '../auth/AuthContext.jsx';

const VIDE = { marque: '', modele: '', couleur: '', immatriculation: '', nombrePlaces: 4 };

export default function Vehicles() {
  const { refreshUser } = useAuth();
  const [vehicules, setVehicules] = useState([]);
  const [form, setForm] = useState(VIDE);
  const [erreur, setErreur] = useState('');

  const charger = () => api.get('/api/vehicules').then(({ data }) => setVehicules(data));

  useEffect(() => {
    charger().catch(() => {});
  }, []);

  const set = (champ) => (e) =>
    setForm({ ...form, [champ]: champ === 'nombrePlaces' ? Number(e.target.value) : e.target.value });

  const ajouter = async (e) => {
    e.preventDefault();
    setErreur('');
    try {
      await api.post('/api/vehicules', form);
      setForm(VIDE);
      await charger();
      await refreshUser(); 
    } catch {
      setErreur('Ajout impossible');
    }
  };

  const supprimer = async (id) => {
    await api.delete(`/api/vehicules/${id}`).catch(() => {});
    charger();
  };

  return (
    <div className="page-center">
      <div className="card">
        <h2>Mes véhicules</h2>
        {vehicules.length === 0 && <p className="muted">Aucun véhicule enregistré.</p>}
        {vehicules.map((v) => (
          <div key={v.id} className="info-row">
            <span>
              {v.marque} {v.modele} · {v.nombrePlaces} places
              {v.couleur ? ` · ${v.couleur}` : ''}
            </span>
            <button className="link-danger" onClick={() => supprimer(v.id)}>
              Supprimer
            </button>
          </div>
        ))}
      </div>

      <div className="card" style={{ marginTop: 20 }}>
        <h2>Ajouter un véhicule</h2>
        <form onSubmit={ajouter}>
          <div className="grid-2">
            <div>
              <label className="label">Marque</label>
              <input className="field" value={form.marque} onChange={set('marque')} required />
            </div>
            <div>
              <label className="label">Modèle</label>
              <input className="field" value={form.modele} onChange={set('modele')} required />
            </div>
            <div>
              <label className="label">Couleur</label>
              <input className="field" value={form.couleur} onChange={set('couleur')} />
            </div>
            <div>
              <label className="label">Immatriculation</label>
              <input className="field" value={form.immatriculation} onChange={set('immatriculation')} />
            </div>
            <div>
              <label className="label">Nombre de places</label>
              <input
                className="field"
                type="number"
                min="1"
                max="8"
                value={form.nombrePlaces}
                onChange={set('nombrePlaces')}
              />
            </div>
          </div>
          {erreur && <p className="error">{erreur}</p>}
          <button className="btn btn-primary" type="submit">
            Ajouter
          </button>
        </form>
      </div>
    </div>
  );
}
