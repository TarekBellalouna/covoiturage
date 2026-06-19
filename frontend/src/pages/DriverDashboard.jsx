import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/client.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { useStomp } from "../ws/StompProvider.jsx";
import MapView from "../components/MapView.jsx";
import Avatar from "../components/Avatar.jsx";
import RatingModal from "../components/RatingModal.jsx";

const FALLBACK = { latitude: 36.7538, longitude: 3.0588 }; // Alger, si la geoloc est refusee

export default function DriverDashboard() {
  const { user } = useAuth();
  const { connected, subscribe } = useStomp();
  const navigate = useNavigate();

  const [vehicules, setVehicules] = useState([]);
  const [vehiculeId, setVehiculeId] = useState("");
  const [disponible, setDisponible] = useState(false);
  const [demandes, setDemandes] = useState([]);
  const [trajet, setTrajet] = useState(null);
  const [erreur, setErreur] = useState("");
  const [trajetRoutes, setTrajetRoutes] = useState([]);
  const [demandeAAccepter, setDemandeAAccepter] = useState(null);
  const [prix, setPrix] = useState("");
  const [fileNotes, setFileNotes] = useState([]);
  const [maPosition, setMaPosition] = useState(null);

  const posRef = useRef(FALLBACK);

  const demanderPosition = () =>
    new Promise((resolve) => {
      if (!navigator.geolocation) return resolve(posRef.current);

      navigator.geolocation.getCurrentPosition(
        (p) => {
          const coords = {
            latitude: p.coords.latitude,
            longitude: p.coords.longitude,
          };
          posRef.current = coords;
          setMaPosition([coords.latitude, coords.longitude]);
          resolve(coords);
        },
        () => resolve(posRef.current),
        {
          enableHighAccuracy: true,
          maximumAge: 0,
          timeout: 10000,
        },
      );
    });

  const chargerDemandesProches = async (conducteurDisponible) => {
    const coords = await demanderPosition();
    await api.patch("/api/conducteurs/me/position", {
      ...coords,
      disponible: conducteurDisponible,
    });
    const { data } = await api.get("/api/demandes/proximite");
    setDemandes(data);
  };

  useEffect(() => {
    api.get("/api/vehicules").then(({ data }) => {
      setVehicules(data);
      if (data.length) setVehiculeId(String(data[0].id));
    });
    api
      .get("/api/trajets/me")
      .then(({ data }) => {
        const actif = data.find(
          (t) => t.statut === "OUVERT" || t.statut === "EN_COURS",
        );
        if (actif) {
          setTrajet(actif);
          chargerRoutesTrajet(actif.id);
        }
      })
      .catch(() => {});

    chargerDemandesProches(false).catch(() => {});
  }, []);

  // Reception temps reel des demandes proches (jamais les siennes).
  useEffect(() => {
    if (!connected) return;
    const unsub = subscribe("/user/queue/demandes", (d) => {
      if (d.passagerId === user?.id) return;
      setDemandes((prev) =>
        prev.some((x) => x.id === d.id) ? prev : [d, ...prev],
      );
    });
    return unsub;
  }, [connected, subscribe, user]);

  // Remontee periodique de la position quand le conducteur est en ligne.
  useEffect(() => {
    if (!disponible) return;
    const envoyer = async () => {
      const coords = await demanderPosition();
      api
        .patch("/api/conducteurs/me/position", { ...coords, disponible: true })
        .catch(() => {});
    };
    envoyer();
    const id = setInterval(envoyer, 12000);
    return () => clearInterval(id);
  }, [disponible]);

  const basculerDisponibilite = async () => {
    const prochain = !disponible;
    setDisponible(prochain);
    if (!prochain) {
      setDemandes([]); // hors ligne : plus de demandes
      api
        .patch("/api/conducteurs/me/position", {
          ...posRef.current,
          disponible: false,
        })
        .catch(() => {});
      return;
    }
    // En ligne : on demande la position actuelle, on l'enregistre, puis on charge les demandes proches.
    try {
      await chargerDemandesProches(true);
    } catch {
      () => {};
    }
  };

  const accepter = async (demandeId, prixSaisi) => {
    setErreur("");
    try {
      const { data } = await api.post(`/api/demandes/${demandeId}/accepter`, {
        vehiculeId: Number(vehiculeId),
        trajetId: trajet?.id ?? null,
        prix: Number(prixSaisi),
      });
      setDemandes((prev) => prev.filter((d) => d.id !== demandeId));
      if (data.trajetId) {
        const { data: t } = await api.get(`/api/trajets/${data.trajetId}`);
        setTrajet(t);
        chargerRoutesTrajet(t.id);
      }
    } catch (e) {
      setErreur(
        e?.response?.data?.message || "Impossible d’accepter cette demande",
      );
    }
  };

  const refuser = (demandeId) =>
    setDemandes((prev) => prev.filter((d) => d.id !== demandeId));

  const actionTrajet = async (action) => {
    if (!trajet) return;
    try {
      const { data } = await api.post(`/api/trajets/${trajet.id}/${action}`);
      // A la fin du trajet : on ouvre directement la notation des passagers.
      if (action === "terminer") {
        try {
          const { data: det } = await api.get(
            `/api/trajets/${trajet.id}/details`,
          );
          const file = det.passagers
            .filter((p) => p.statut === "TERMINEE")
            .map((p) => ({
              trajetId: trajet.id,
              cibleId: p.id,
              titre: `Noter ${p.prenom}`,
            }));
          setFileNotes(file);
        } catch {
          () => {};
        }
      }
      if (data.statut === "TERMINE" || data.statut === "ANNULE") {
        setTrajet(null);
        setTrajetRoutes([]);
      } else {
        setTrajet(data);
      }
    } catch (e) {
      setErreur(e?.response?.data?.message || "Action impossible");
    }
  };

  const chargerRoutesTrajet = async (trajetId) => {
    try {
      const { data } = await api.get(`/api/trajets/${trajetId}/details`);
      const depart = [data.pointDepart.latitude, data.pointDepart.longitude];
      const rts = data.passagers
        .filter((p) => p.destinationLat != null && p.destinationLng != null)
        .map((p) => [depart, [p.destinationLat, p.destinationLng]]);
      setTrajetRoutes(rts);
    } catch {
      setTrajetRoutes([]);
    }
  };

  const markers = demandes.map((d) => ({
    id: d.id,
    lat: d.latitude,
    lng: d.longitude,
    label: `${d.pointRencontreNom} → ${d.destinationTexte}`,
    color: "#ba7517",
  }));

  const routes = demandes
    .filter((d) => d.destinationLat != null && d.destinationLng != null)
    .map((d) => [
      [d.latitude, d.longitude],
      [d.destinationLat, d.destinationLng],
    ]);

  const centre = trajetRoutes.length
    ? trajetRoutes[0][0]
    : demandes.length
      ? [demandes[0].latitude, demandes[0].longitude]
      : (maPosition ?? undefined);

  if (vehicules.length === 0) {
    return (
      <div className="split">
        <aside className="sidebar">
          <h2>Mode conducteur</h2>
          <p className="muted">
            Pour recevoir des demandes, ajoutez d’abord un véhicule.
          </p>
          <Link
            className="btn btn-primary"
            to="/vehicules"
            style={{ display: "inline-block", textAlign: "center" }}
          >
            Ajouter un véhicule
          </Link>
        </aside>
        <div className="map-area">
          <MapView
            markers={markers}
            routes={routes}
            tripRoutes={trajetRoutes}
            center={centre}
          />{" "}
        </div>
      </div>
    );
  }

  return (
    <div className="home">
      <section className="home-panel">
        <div className="row-between">
          <h2 style={{ margin: 0 }}>Conducteur</h2>
          <button
            className={`toggle ${disponible ? "toggle-on" : ""}`}
            onClick={basculerDisponibilite}
            aria-label="Disponibilité"
            title="Se mettre en ligne / hors ligne"
          >
            <span className="toggle-knob" />
          </button>
        </div>
        <p className="muted">
          {disponible
            ? "En ligne — vous recevez les demandes"
            : "Hors ligne — aucune demande reçue"}
        </p>

        <label className="label">Véhicule</label>
        <select
          className="field"
          value={vehiculeId}
          onChange={(e) => setVehiculeId(e.target.value)}
        >
          {vehicules.map((v) => (
            <option key={v.id} value={v.id}>
              {v.marque} {v.modele} · {v.nombrePlaces} places
            </option>
          ))}
        </select>

        {erreur && <p className="error">{erreur}</p>}

        {trajet && (
          <div className="status-card" style={{ marginTop: 16 }}>
            <p style={{ margin: 0, fontWeight: 500 }}>Trajet en cours</p>
            <span className={`badge badge-${trajet.statut}`}>
              {trajet.statut}
            </span>
            <p className="muted">
              {trajet.nombrePassagers} passager(s) · départ{" "}
              {trajet.pointDepartNom}
            </p>
            {trajet.statut === "OUVERT" && (
              <button
                className="btn btn-primary"
                onClick={() => actionTrajet("demarrer")}
              >
                Démarrer le trajet
              </button>
            )}
            {trajet.statut === "EN_COURS" && (
              <button
                className="btn btn-primary"
                onClick={() => actionTrajet("terminer")}
              >
                Terminer le trajet
              </button>
            )}
            <Link
              className="link"
              to={`/trajets/${trajet.id}`}
              style={{ display: "block", marginTop: 8 }}
            >
              Voir le détail
            </Link>
            <button
              className="btn btn-danger"
              onClick={() => actionTrajet("annuler")}
            >
              Annuler
            </button>
          </div>
        )}

        <h3 style={{ fontSize: 15, marginTop: 20 }}>Demandes à proximité</h3>
        {demandes.length === 0 && (
          <p className="muted">Aucune demande pour le moment.</p>
        )}
        {demandes.map((d) => (
          <div key={d.id} className="demande-card">
            <button
              className="passager-head"
              onClick={() => navigate(`/users/${d.passagerId}`)}
              title="Voir le profil"
            >
              <Avatar
                photoUrl={d.passagerPhotoUrl}
                initiales={(d.passagerPrenom?.[0] || "").toUpperCase()}
                size={40}
              />
              <span className="passager-info">
                <span className="passager-nom">{d.passagerPrenom}</span>
                <span className="muted">
                  {d.passagerTelephone || "Téléphone non renseigné"}
                </span>
              </span>
            </button>
            <p className="muted" style={{ margin: "8px 0 0" }}>
              {d.pointRencontreNom} → {d.destinationTexte}
            </p>
            <p className="muted" style={{ margin: "2px 0 8px" }}>
              {d.nombrePlacesDemandees} place(s)
            </p>
            <div style={{ display: "flex", gap: 8 }}>
              <button
                className="btn"
                style={{ marginTop: 0, flex: 1 }}
                onClick={() => refuser(d.id)}
              >
                Refuser
              </button>
              <button
                className="btn btn-primary"
                style={{ marginTop: 0, flex: 1 }}
                onClick={() => {
                  setDemandeAAccepter(d);
                  setPrix("");
                }}
              >
                Accepter
              </button>
            </div>
          </div>
        ))}
      </section>

      <div className="home-map">
        <MapView
          markers={markers}
          routes={routes}
          tripRoutes={trajetRoutes}
          center={centre}
        />{" "}
      </div>

      {fileNotes.length > 0 && (
        <RatingModal
          key={fileNotes[0].cibleId}
          trajetId={fileNotes[0].trajetId}
          cibleId={fileNotes[0].cibleId}
          titre={fileNotes[0].titre}
          onClose={() => setFileNotes((f) => f.slice(1))}
        />
      )}

      {demandeAAccepter && (
        <div
          className="modal-backdrop"
          onClick={() => setDemandeAAccepter(null)}
        >
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>Prix du trajet</h2>
            <p className="muted">
              {demandeAAccepter.pointRencontreNom} →{" "}
              {demandeAAccepter.destinationTexte}
            </p>
            <label className="label">Prix (€)</label>
            <input
              className="field"
              type="number"
              min="0"
              step="0.5"
              value={prix}
              onChange={(e) => setPrix(e.target.value)}
              placeholder="Ex. 5"
            />
            <div style={{ display: "flex", gap: 10, marginTop: 14 }}>
              <button
                className="btn"
                style={{ flex: 1 }}
                onClick={() => setDemandeAAccepter(null)}
              >
                Annuler
              </button>
              <button
                className="btn btn-primary"
                style={{ flex: 1 }}
                disabled={!prix || Number(prix) <= 0}
                onClick={() => {
                  accepter(demandeAAccepter.id, prix);
                  setDemandeAAccepter(null);
                }}
              >
                Confirmer
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
