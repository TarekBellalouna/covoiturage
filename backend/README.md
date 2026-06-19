# Covoiturage — Backend (Spring Boot)

Backend de l'application de covoiturage (projet TP), inspiré d'Uber : demandes de
trajet en temps réel, mise en relation des passagers avec les conducteurs proches.

## Stack

- **Spring Boot 4.0.6** (Spring Framework 7 / Spring Security 7) · **Java 21**
- **PostgreSQL 16 + PostGIS 3.4** (recherche géographique par rayon)
- **JWT** (jjwt 0.13.0) — authentification stateless
- **WebSocket / STOMP** — notifications temps réel
- **Lombok** — réduction du boilerplate

> Lombok : pensez à installer le plugin/extension Lombok de votre IDE
> (IntelliJ ou VS Code). La compilation via Maven fonctionne sans plugin.

## Domaines

```
com/covoiturage/
├── config/         SecurityConfig, WebSocketConfig
├── security/       JwtService, JwtAuthenticationFilter, StompAuthChannelInterceptor
├── common/         GlobalExceptionHandler, DataInitializer (points de rencontre)
├── notification/   NotificationService (push WebSocket)
├── user/           Utilisateur, ModeUtilisateur, repo, controller (/me, /me/mode)
├── auth/           register / login (émission du JWT)
├── vehicule/       CRUD véhicules (le 1er débloque le rôle conducteur)
├── pointrencontre/ points de rencontre (lecture)
├── position/       remontée de position + recherche PostGIS des conducteurs proches
├── demande/        DemandeCovoiturage : création, acceptation atomique, expiration
├── trajet/         Trajet : démarrer / terminer / annuler (covoiturage partagé)
└── evaluation/     notation réciproque + réputation
```

## Démarrage

```bash
docker compose up -d      # PostgreSQL + PostGIS + Adminer
mvn spring-boot:run       # API sur http://localhost:8080
```

Au premier lancement, quelques points de rencontre sont insérés automatiquement.

### Visualiser la base (Adminer)

Adminer est disponible sur **http://localhost:8081**. Connexion :

| Champ | Valeur |
|---|---|
| Système | PostgreSQL |
| Serveur | `db` (le nom du service Docker, **pas** `localhost`) |
| Utilisateur | `covoiturage` |
| Mot de passe | `covoiturage` |
| Base | `covoiturage` |

## Développement dans VS Code

Le projet inclut une config `.vscode/` :

- **Rechargement à chaud (DevTools)** : à chaque enregistrement d'un fichier, VS Code
  recompile la classe et Spring Boot redémarre automatiquement en 1 à 2 s — pas besoin
  de relancer manuellement. Lancez l'application via le bouton *Run* (Spring Boot
  Dashboard) ou `mvn spring-boot:run`, puis modifiez et sauvegardez.
- **Détection des erreurs en direct** : installez les extensions recommandées
  (VS Code les propose à l'ouverture du projet). *Extension Pack for Java* souligne en
  rouge les fautes de syntaxe et de compilation en temps réel ; *Spring Boot Extension
  Pack* ajoute le support Spring ; *Checkstyle* (optionnel) signale les écarts de style.
- Le support **Lombok** est intégré à l'extension Java — aucune extension séparée.
- **Prettier** formate les fichiers de config et docs (JSON, YAML, Markdown). Il ne
  formate pas le Java (réservé à l'extension Java) ; son vrai rôle viendra avec le front React.

## Aperçu des endpoints

| Méthode | Endpoint | Rôle |
|---|---|---|
| POST | `/api/auth/register` · `/login` | public |
| GET | `/api/users/me` | authentifié |
| PATCH | `/api/users/me/mode` | conducteur |
| GET/POST/PATCH/DELETE | `/api/vehicules` … | conducteur |
| GET | `/api/points-rencontre` | authentifié |
| PATCH | `/api/conducteurs/me/position` | conducteur |
| POST | `/api/demandes` | passager |
| GET | `/api/demandes/me` · `/proximite` · `/{id}` | concerné |
| POST | `/api/demandes/{id}/annuler` · `/accepter` | concerné |
| GET | `/api/trajets/me` · `/{id}` | concerné |
| POST | `/api/trajets/{id}/demarrer` · `/terminer` · `/annuler` | conducteur |
| POST | `/api/trajets/{id}/evaluations` | concerné |
| GET | `/api/users/{id}/reputation` | authentifié |

### WebSocket

- Endpoint STOMP : `/ws` (JWT dans l'en-tête `Authorization` du frame CONNECT)
- Conducteur : abonnement à `/user/queue/demandes` (nouvelles demandes proches)
- Passager : abonnement à `/user/queue/suivi` (acceptation + état du trajet)

## Points de conception

- **Concurrence** : la transition `EN_ATTENTE → ACCEPTEE` est protégée par un verrou
  optimiste (`@Version`) — si deux conducteurs acceptent en même temps, le second
  reçoit `409 Conflict`.
- **Recherche géographique** : `ST_DWithin` (PostGIS) en SQL natif, rayon de 4 km.
- **Covoiturage partagé** : un `Trajet` reste `OUVERT` et peut embarquer plusieurs
  demandes tant qu'il reste des places.
- **Expiration** : une tâche `@Scheduled` passe en `EXPIREE` les demandes sans réponse.
- Sécurité stateless (JWT), mots de passe en BCrypt, dates en ISO-8601 (Jackson 3).

## Notes

- `spring.jpa.hibernate.ddl-auto=update` en dev ; passer à `validate` + Flyway ensuite.
- Surcharger le secret JWT via la variable d'environnement `JWT_SECRET` (≥ 32 caractères).

## Endpoints ajoutés (profils, détail, notifications)

- `GET /api/users/{id}` — profil public d'un utilisateur (nom, téléphone, réputation)
- `GET /api/trajets/{id}/details` — détail complet d'un trajet (conducteur, véhicule,
  passagers, évaluations) ; réservé au conducteur ou à un passager du trajet
- `GET /api/notifications` — liste des notifications de l'utilisateur
- `GET /api/notifications/non-lues` — nombre de notifications non lues
- `POST /api/notifications/lu-tout` — marquer toutes les notifications comme lues

Les notifications sont persistées en base **et** poussées en temps réel sur
`/user/queue/notifications` (acceptation, démarrage, fin, annulation, nouvelle demande).

- `PATCH /api/users/me/profil` — modifier son profil (nom, prénom, téléphone, photo en data URL)
- `GET /api/carte/activite` — demandes en attente + trajets actifs pour la carte (avec statut)

> La photo de profil est stockée en colonne `TEXT` (data URL base64). Sur une base déjà
> créée, pensez à recréer la table `utilisateurs` ou exécuter
> `ALTER TABLE utilisateurs ALTER COLUMN photo_url TYPE TEXT;`.
