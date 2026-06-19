import { useRef, useState } from 'react';

export default function DestinationAutocomplete({ value, onSelect }) {
  const [texte, setTexte] = useState(value || '');
  const [suggestions, setSuggestions] = useState([]);
  const timer = useRef(null);

  const chercher = (q) => {
    setTexte(q);
    onSelect(null); // tant qu'on n'a pas cliqué dans la liste : pas de coordonnées
    if (timer.current) clearTimeout(timer.current);
    if (q.trim().length < 3) return setSuggestions([]);
    timer.current = setTimeout(async () => {
      try {
        const r = await fetch(
          `https://api-adresse.data.gouv.fr/search/?q=${encodeURIComponent(q)}&limit=5`
        );
        const data = await r.json();
        setSuggestions(data.features || []);
      } catch {
        setSuggestions([]);
      }
    }, 300);
  };

  const choisir = (f) => {
    const [lng, lat] = f.geometry.coordinates; // l'API renvoie [lon, lat]
    setTexte(f.properties.label);
    setSuggestions([]);
    onSelect({ label: f.properties.label, lat, lng });
  };

  return (
    <div className="autocomplete">
      <input
        className="field"
        placeholder="Où allez-vous ?"
        value={texte}
        onChange={(e) => chercher(e.target.value)}
      />
      {suggestions.length > 0 && (
        <ul className="suggestions">
          {suggestions.map((f) => (
            <li key={f.properties.id} onClick={() => choisir(f)}>
              {f.properties.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}