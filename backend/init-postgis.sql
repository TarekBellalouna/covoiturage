-- Active l'extension PostGIS sur la base au premier demarrage du conteneur.
-- (L'image postgis/postgis le fait deja, mais on le rend explicite.)
CREATE EXTENSION IF NOT EXISTS postgis;
