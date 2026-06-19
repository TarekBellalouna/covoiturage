# Covoiturage — pile complète (Docker)

Lance toute l'application avec une seule commande : base de données PostGIS,
backend Spring Boot et frontend React.

## Lancer

Depuis ce dossier (celui qui contient `docker-compose.yml`) :

```bash
docker compose up --build
```

Le premier démarrage est long (compilation Maven + build Vite). Une fois prêt :

| Service   | URL                     |
|-----------|-------------------------|
| Frontend  | http://localhost:5173   |
| Backend   | http://localhost:8080   |
| Adminer   | http://localhost:8081   |

Pour arrêter : `Ctrl+C`, puis `docker compose down`.
Pour repartir d'une base vierge : `docker compose down -v` (supprime le volume).

## Architecture

```
covoiturage/
├── docker-compose.yml      db + backend + frontend (+ adminer)
├── backend/                Spring Boot 4 / Java 21
│   └── Dockerfile          build Maven → image JRE
└── frontend/               React 19 / Vite 8
    ├── Dockerfile          build Vite → nginx
    └── nginx.conf          service statique + fallback SPA
```

- Le **frontend** est servi par nginx sur le port 5173. Le navigateur appelle le
  backend sur `http://localhost:8080` (l'origine `http://localhost:5173` est autorisée
  par le CORS du backend).
- Le **backend** se connecte à la base via le réseau Docker (`db:5432`), surchargé par
  les variables `SPRING_DATASOURCE_*`.
- La **base** persiste dans le volume `covoiturage-pgdata` et active PostGIS au premier
  démarrage via `backend/init-postgis.sql`.

## Variables à connaître

- `JWT_SECRET` — secret de signature des tokens (à remplacer en production)
- `APP_CORS_ALLOWED_ORIGINS` — origines autorisées (par défaut `http://localhost:5173`)

## Développement sans Docker

Le dossier `backend/` garde son propre `docker-compose.yml` (base + Adminer seulement),
pratique pour lancer le backend avec `mvn spring-boot:run` et le frontend avec
`npm run dev`.
