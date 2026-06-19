import { useState } from 'react';
import api from '../api/client.js';

export default function RatingModal({ trajetId, titre = 'Noter le trajet', cibleId = null, onClose }) {
  const [note, setNote] = useState(5);
  const [commentaire, setCommentaire] = useState('');
  const [erreur, setErreur] = useState('');
  const [enCours, setEnCours] = useState(false);

  const envoyer = async () => {
    setEnCours(true);
    setErreur('');
    try {
      await api.post(`/api/trajets/${trajetId}/evaluations`, { note, commentaire, cibleId });
      onClose(true);
    } catch (e) {
      setErreur(e?.response?.data?.message || 'Notation impossible');
      setEnCours(false);
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="modal">
        <h2>{titre}</h2>
        <div className="stars">
          {[1, 2, 3, 4, 5].map((n) => (
            <button
              key={n}
              type="button"
              className={`star ${n <= note ? 'star-on' : ''}`}
              onClick={() => setNote(n)}
              aria-label={`${n} étoile(s)`}
            >
              ★
            </button>
          ))}
        </div>
        <label className="label">Commentaire (facultatif)</label>
        <textarea
          className="field"
          style={{ height: 80, padding: 10 }}
          value={commentaire}
          onChange={(e) => setCommentaire(e.target.value)}
        />
        {erreur && <p className="error">{erreur}</p>}
        <div style={{ display: 'flex', gap: 10 }}>
          <button className="btn" style={{ flex: 1 }} onClick={() => onClose(false)}>
            Plus tard
          </button>
          <button className="btn btn-primary" style={{ flex: 1 }} onClick={envoyer} disabled={enCours}>
            {enCours ? 'Envoi…' : 'Envoyer'}
          </button>
        </div>
      </div>
    </div>
  );
}
