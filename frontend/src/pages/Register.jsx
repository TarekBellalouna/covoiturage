import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    prenom: '',
    nom: '',
    email: '',
    motDePasse: '',
    telephone: '',
  });
  const [erreur, setErreur] = useState('');
  const [enCours, setEnCours] = useState(false);

  const set = (champ) => (e) => setForm({ ...form, [champ]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setErreur('');
    setEnCours(true);
    try {
      await register(form);
      navigate('/');
    } catch {
      setErreur('Inscription impossible (email déjà utilisé ?)');
    } finally {
      setEnCours(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="brand">Créer un compte</h1>
        <p className="muted">Vous commencez comme passager</p>
        <form onSubmit={onSubmit}>
          <label className="label">Prénom</label>
          <input className="field" value={form.prenom} onChange={set('prenom')} required />
          <label className="label">Nom</label>
          <input className="field" value={form.nom} onChange={set('nom')} required />
          <label className="label">Email</label>
          <input className="field" type="email" value={form.email} onChange={set('email')} required />
          <label className="label">Téléphone</label>
          <input className="field" value={form.telephone} onChange={set('telephone')} />
          <label className="label">Mot de passe</label>
          <input
            className="field"
            type="password"
            value={form.motDePasse}
            onChange={set('motDePasse')}
            minLength={6}
            required
          />
          {erreur && <p className="error">{erreur}</p>}
          <button className="btn btn-primary" type="submit" disabled={enCours}>
            {enCours ? 'Création…' : 'Créer mon compte'}
          </button>
        </form>
        <p className="muted center" style={{ marginTop: '1rem' }}>
          Déjà inscrit ? <Link to="/login">Se connecter</Link>
        </p>
      </div>
    </div>
  );
}
