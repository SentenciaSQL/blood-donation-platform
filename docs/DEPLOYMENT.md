# Despliegue — Blood Donation Platform

## Respuesta corta sobre Firebase

**Firebase Hosting solo sirve para el frontend (Angular/HTML estático).**

Este proyecto es:

| Pieza | Tecnología | ¿Cabe en Firebase Hosting? |
|-------|------------|----------------------------|
| API | Spring Boot (Java 21) | **No** |
| Base de datos | PostgreSQL | **No** |
| UI | Angular (aún pendiente Fase 13+) | **Sí** |

Arquitectura recomendada “estilo Firebase”:

```text
Usuario
  │
  ├─ Firebase Hosting  →  Angular (SPA)
  │
  └─ Cloud Run / Railway / Render  →  Spring Boot API
           │
           └─ Neon / Cloud SQL / Railway Postgres  →  PostgreSQL
```

---

## Opción A — Más sencilla (recomendada para portafolio)

Usa un PaaS que soporte Java + Postgres (sin Firebase):

### Railway / Render / Fly.io

1. Crea un servicio **PostgreSQL**.
2. Despliega el backend con el `Dockerfile` de este repo.
3. Configura variables de entorno (ver abajo).
4. Cuando exista el frontend Angular, desplégalo en Firebase Hosting o en el mismo PaaS como sitio estático.

Comando local equivalente:

```bash
docker compose up --build
```

API: `http://localhost:8080`  
Swagger: `http://localhost:8080/swagger-ui.html`

---

## Opción B — Firebase Hosting + Google Cloud Run

### 1) Base de datos

Crea Postgres administrado, por ejemplo:

- [Neon](https://neon.tech) (gratis / fácil), o
- Cloud SQL for PostgreSQL

Copia la URL JDBC:

```text
jdbc:postgresql://HOST:5432/blood_donation_db?sslmode=require
```

### 2) Backend en Cloud Run

```bash
# Autenticación GCP
gcloud auth login
gcloud config set project YOUR_GCP_PROJECT_ID

# Build y push (Artifact Registry)
gcloud builds submit --tag REGION-docker.pkg.dev/YOUR_GCP_PROJECT_ID/blood/api:latest

# Deploy
gcloud run deploy blood-donation-api \
  --image REGION-docker.pkg.dev/YOUR_GCP_PROJECT_ID/blood/api:latest \
  --region REGION \
  --allow-unauthenticated \
  --port 8080 \
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod,SPRING_FLYWAY_ENABLED=true,SPRING_JPA_HIBERNATE_DDL_AUTO=validate,SERVER_PORT=8080,JWT_EXPIRATION_MINUTES=60,JWT_REFRESH_EXPIRATION_DAYS=7,CORS_ALLOWED_ORIGINS=https://YOUR_FIREBASE_DOMAIN" \
  --set-secrets "SPRING_DATASOURCE_URL=DB_URL:latest,SPRING_DATASOURCE_USERNAME=DB_USER:latest,SPRING_DATASOURCE_PASSWORD=DB_PASSWORD:latest,JWT_SECRET=JWT_SECRET:latest"
```

Guarda la URL pública del servicio Cloud Run (ej. `https://blood-donation-api-xxxxx.run.app`).

### 3) Frontend en Firebase Hosting (cuando exista Angular)

```bash
npm install -g firebase-tools
firebase login
cp .firebaserc.example .firebaserc
# Edita .firebaserc con tu project id

cd frontend
npm ci
npm run build
cd ..

firebase deploy --only hosting
```

En el frontend, apunta la API a la URL de Cloud Run:

```ts
// environment.prod.ts (ejemplo)
export const environment = {
  production: true,
  apiUrl: 'https://blood-donation-api-xxxxx.run.app'
};
```

---

## Variables de entorno requeridas

| Variable | Ejemplo |
|----------|---------|
| `SERVER_PORT` | `8080` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://.../blood_donation_db` |
| `SPRING_DATASOURCE_USERNAME` | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | *(secreto)* |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `validate` |
| `SPRING_FLYWAY_ENABLED` | `true` |
| `JWT_SECRET` | ≥ 32 caracteres aleatorios |
| `JWT_EXPIRATION_MINUTES` | `60` |
| `JWT_REFRESH_EXPIRATION_DAYS` | `7` |
| `CORS_ALLOWED_ORIGINS` | `https://tu-app.web.app` |
| `SPRING_PROFILES_ACTIVE` | `prod` |

Nunca subas secretos al repositorio. Usa Secret Manager / variables del hosting.

---

## Estado actual del proyecto

- Backend API: listo (Fases 1–8 en PR `#4`)
- Frontend Angular: **aún no creado** (Fase 13+)
- Por eso **hoy solo puedes desplegar la API** (Cloud Run / Railway / Docker)
- Firebase Hosting tendrá sentido cuando exista `frontend/`

---

## Checklist rápido

1. [ ] Mergear PR de fases 2–8
2. [ ] Crear Postgres gestionado
3. [ ] Desplegar API con Docker
4. [ ] Probar `/swagger-ui.html` y `/auth/login`
5. [ ] Completar frontend Angular
6. [ ] Desplegar SPA en Firebase Hosting
7. [ ] Configurar `CORS_ALLOWED_ORIGINS` con el dominio Firebase

---

## Alternativas “tipo Firebase” (todo-en-uno)

Si quieres algo más simple que GCP:

| Hosting | Backend Java | Postgres | Frontend |
|---------|--------------|----------|----------|
| **Railway** | Sí | Sí | Sí |
| **Render** | Sí | Sí | Sí |
| **Fly.io** | Sí | Sí | Sí |
| **Firebase Hosting** | No | No | Sí |

Para un portafolio rápido: **Railway (API + Postgres) + Firebase Hosting (Angular)** o todo en Railway.
