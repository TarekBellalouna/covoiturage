# Covoiturage — Frontend (React, version ordinateur)

Interface web (desktop) de l'application de covoiturage, connectée au backend Spring Boot.

## Stack

- **React 19** + **Vite 8** + **react-router-dom 7**
- **axios** (API, injection automatique du JWT)
- **Leaflet** + **react-leaflet** (carte OpenStreetMap)
- **@stomp/stompjs** + **sockjs-client** (temps réel WebSocket / STOMP)
- **ESLint 9** + **Prettier**

> Vite 8 nécessite **Node.js 20.19+ ou 22.12+**.

## Démarrage

```bash
npm install
npm run dev          # http://localhost:5173
```

Le backend doit tourner sur `http://localhost:8090`.
Pour pointer ailleurs, définir `VITE_API_URL` dans un fichier `.env`.

## Structure

```
src/
├── main.jsx                    entrée (Router + Auth + Stomp + Notifications)
├── App.jsx                     routes
├── styles.css                  système de design
├── api/client.js               axios + injection du token
├── auth/AuthContext.jsx        authentification
├── ws/
│   ├── StompProvider.jsx       connexion WebSocket/STOMP + abonnements
│   └── NotificationProvider.jsx centre de notifications (liste + compteur)
├── components/
│   ├── ProtectedRoute.jsx
│   ├── AppShell.jsx            navbar + cloche de notifications
│   ├── MapView.jsx             carte Leaflet réutilisable
│   ├── RatingModal.jsx         fenêtre de notation
│   ├── UserProfileModal.jsx    profil public (passager / conducteur)
│   └── NotificationBell.jsx    dropdown de notifications
└── pages/
    ├── Login.jsx · Register.jsx
    ├── PassengerHome.jsx       carte + demande + suivi + profil conducteur
    ├── DriverDashboard.jsx     disponibilité, demandes, profil passager
    ├── Vehicles.jsx            gestion des véhicules
    ├── History.jsx             historique cliquable
    ├── TripDetail.jsx          détail complet d'un trajet + notation
    └── Profile.jsx             profil, réputation, bascule de mode
```

## Fonctionnalités

- Authentification (JWT, routes protégées)
- Carte interactive (points de rencontre, position des demandes)
- Passager : demande, suivi temps réel, consultation du profil du conducteur, notation
- Conducteur : disponibilité, position, demandes temps réel, consultation du profil du
  passager, acceptation, gestion du trajet, notation des passagers
- Véhicules : ajout / suppression
- Historique cliquable menant à une page **détail** (conducteur, véhicule, passagers,
  notes et commentaires)
- **Notifications** persistées et temps réel : dropdown sur l'icône de profil avec
  compteur de non-lues
