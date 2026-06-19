import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [motDePasse, setMotDePasse] = useState('');
  const [erreur, setErreur] = useState('');
  const [enCours, setEnCours] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    setErreur('');
    setEnCours(true);
    try {
      await login(email, motDePasse);
      navigate('/');
    } catch {
      setErreur('Identifiants invalides');
    } finally {
      setEnCours(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="brand">Covoiturage</h1>
        <p className="muted">Connectez-vous pour continuer</p>
        <form onSubmit={onSubmit}>
          <label className="label">Email</label>
          <input
            className="field"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <label className="label">Mot de passe</label>
          <input
            className="field"
            type="password"
            value={motDePasse}
            onChange={(e) => setMotDePasse(e.target.value)}
            required
          />
          {erreur && <p className="error">{erreur}</p>}
          <button className="btn btn-primary" type="submit" disabled={enCours}>
            {enCours ? 'Connexion…' : 'Se connecter'}
          </button>
        </form>
        <p className="muted center" style={{ marginTop: '1rem' }}>
          Pas encore de compte ? <Link to="/register">Créer un compte</Link>
        </p>
      </div>
    </div>
  );
}
