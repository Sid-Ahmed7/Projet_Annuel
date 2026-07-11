# Skaldly

Plateforme d'apprentissage des langues assistée par IA.

🔗 [skaldly.fr](https://skaldly.fr)

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker_Swarm-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![Stripe](https://img.shields.io/badge/Stripe-626CD9?style=for-the-badge&logo=stripe&logoColor=white)
![Traefik](https://img.shields.io/badge/Traefik_v3-24A1C1?style=for-the-badge&logo=traefikproxy&logoColor=white)

## Sommaire

- [À propos](#à-propos)
- [Fonctionnalités](#fonctionnalités)
- [Stack technique](#stack-technique)
- [Installation](#installation)
- [Variables d'environnement](#variables-denvironnement)
- [Documentation API](#documentation-api)
- [Tests d'intégration](#tests-dintégration)
- [Déploiement](#déploiement)
- [Monitoring](#monitoring)
- [Sauvegardes](#sauvegardes)
- [Équipe](#équipe)

## À propos

Skaldly est une application d'apprentissage des langues assistée par l'IA, proposant des exercices variés (QCM, flashcards, matching, tri).

> 🖥️ Frontend : [Skaldly Front](https://github.com/LuzBoger/Projet_Annuel_Front)

## Fonctionnalités

- Exercices interactifs : QCM, flashcards, matching pairs, tri
- Défis 1v1 mais aussi publics ouverts à tous les joueurs
- Génération de défis et d'aide pédagogique par IA (Mistral) avec quota par plan
- Système de révision par répétition 
- Système d'amis
- Authentification JWT + 2FA (TOTP)
- Abonnements via Stripe (plans Free / Premium avec fonctionnalités configurables)
- Modération de contenu 
- Notifications temps réel distribuées (RabbitMQ + STOMP/SSE) et push (VAPID)
- Suppression de compte avec codes de vérification

## Stack technique

**Backend**
- Java 21 / Spring Boot 3.5.7
- PostgreSQL 16 + Flyway
- WebSocket / STOMP
- RabbitMQ
- Quartz Scheduler (clustered, JDBC store)
- JWT + TOTP 2FA
- Backblaze B2 (stockage S3-compatible, SDK v2)
- Mistral AI (génération de leçons et défis)
- HuggingFace Inference API - `unitary/multilingual-toxic-xlm-roberta` (modération de toxicité multilingue)
- Web Push / VAPID (notifications navigateur)

**Infrastructure**
- Docker Swarm (1 manager + 2 workers)
- Traefik v3 (reverse proxy, Let's Encrypt SSL)
- Docker Secrets
- Docker Socket Proxy
- CI/CD via GitHub Actions (build Maven → push Docker Hub → déploiement SSH)

**Monitoring**
- Prometheus / Grafana / cAdvisor
- Sentry + OpenTelemetry

## Installation

### Prérequis

| Outil | Vérification |
|-------|-------------|
| Java 21+ | `java --version` |
| Docker   | `docker --version` |
| Node.js + npm | `node --version` (pour générer les clés VAPID et les hashes BCrypt admin) |

Le backend se lance entièrement via Docker. Il démarre : PostgreSQL + RabbitMQ + Spring Boot (avec hot-reload). Les seed data sont injectées automatiquement.

### 1. Services tiers

Obtenir les clés avant de remplir `application-dev.properties`. Cliquer sur chaque section ci-dessous pour la développer.

<details>
<summary>🔐 JWT & TOTP</summary>
<br>

Générer une valeur pour chaque propriété :

```bash
openssl rand -base64 32
```

| Propriété | Rôle |
|-----------|------|
| `jwt.secret` | Signature des tokens JWT |
| `totp.encryption` | Chiffrement des secrets 2FA |

</details>

<details>
<summary>📧 Gmail (SMTP)</summary>
<br>

1. Connectez-vous à [myaccount.google.com](https://myaccount.google.com), allez dans **Sécurité** et activez la **Vérification en deux étapes** — c'est obligatoire pour pouvoir créer un mot de passe d'application.
2. Toujours dans **Sécurité**, cliquez sur **Mots de passe des applications**, sélectionnez **Mail** et générez le mot de passe.
3. Copiez les **16 caractères** générés et renseignez-les dans `spring.mail.password`.

> 📖 [Guide officiel Google — Mots de passe des applications](https://support.google.com/accounts/answer/185833)

</details>

<details>
<summary>💳 Stripe</summary>
<br>

**Clés API**

Connectez-vous sur [dashboard.stripe.com](https://dashboard.stripe.com), allez dans **Developers** puis **API keys**. Vous y trouverez votre clé secrète et votre clé publique.

| Propriété | Valeur |
|-----------|--------|
| `stripe.secretKey` | `sk_test_...` |
| `stripe.publicKey` | `pk_test_...` |

**Webhook**

Dans **Developers**, allez dans **Webhooks** et ajoutez un endpoint. En développement, utilisez la CLI Stripe pour rediriger les événements vers votre serveur local :

```bash
stripe listen --forward-to localhost:8080/api/v1/stripe/webhook
```

La CLI affiche le `webhook-secret` à copier dans la propriété suivante :

| Propriété | Valeur |
|-----------|--------|
| `stripe.webhook-secret` | `whsec_...` |

> 📖 [Guide officiel Stripe — Webhooks en local](https://docs.stripe.com/webhooks#test-webhook)

</details>

<details>
<summary>👤 Flyway — compte admin</summary>
<br>

Le compte admin est créé automatiquement par Flyway au premier démarrage. La connexion admin  requiert **deux credentials** vérifiés via BCrypt : un mot de passe et une clé secrète.

**Mot de passe** — doit respecter :

| Règle | Détail |
|-------|--------|
| Longueur minimale | 12 caractères |
| Majuscule | Au moins 1 (`A-Z`) |
| Minuscule | Au moins 1 (`a-z`) |
| Chiffre | Au moins 1 (`0-9`) |
| Caractère spécial | Au moins 1 parmi `@ # $ % ^ & + = !` |
| Espaces | Interdits |

```bash
mkdir -p /tmp/bcrypt && cd /tmp/bcrypt
npm init -y -q && npm install bcryptjs -q
node -e "console.log(require('bcryptjs').hashSync('ton_mot_de_passe', 10))"
```

**Clé secrète** — valeur aléatoire quelconque, à conserver précieusement (requis à chaque connexion admin) :

```bash
# Générer la clé 
openssl rand -base64 32

# Hasher avec bcrypt (stocker ce hash dans le placeholder)
node -e "console.log(require('bcryptjs').hashSync('LA_CLE_GENEREE', 10))"
```

| Propriété | Rôle |
|-----------|------|
| `spring.flyway.placeholders.password` | Hash bcrypt du mot de passe admin |
| `spring.flyway.placeholders.secret_key` | Hash bcrypt de la clé secrète (second credential à la connexion) |

</details>

<details>
<summary>🔔 VAPID — Notifications Push</summary>
<br>



```bash
npx web-push generate-vapid-keys
```

La commande affiche directement la clé publique et la clé privée à copier.

| Propriété | Valeur |
|-----------|--------|
| `vapid.public-key` | Clé publique générée |
| `vapid.private-key` | Clé privée générée |
| `vapid.subject` | `mailto:votre@email.com` |

> 📖 [Documentation web-push](https://github.com/web-push-libs/web-push#usage)

</details>

<details>
<summary>🤗 HuggingFace</summary>
<br>

Utilisé pour la modération de contenu via le modèle `unitary/multilingual-toxic-xlm-roberta` (XLM-RoBERTa, entraîné sur le challenge Jigsaw Multilingual). L'API d'inférence gratuite suffit pour le développement.

1. Créez un compte sur [huggingface.co](https://huggingface.co)
2. Allez dans **Settings** puis **Access Tokens**
3. Cliquez sur **New token**, choisissez le rôle **Read** et générez le token

| Propriété | Valeur |
|-----------|--------|
| `huggingface.api-key` | `hf_...` |

> 📖 [Guide officiel HuggingFace — Access Tokens](https://huggingface.co/docs/hub/security-tokens)

</details>

<details>
<summary>🧠 Mistral AI</summary>
<br>

Utilisé pour la génération de leçons et de défis par IA, ainsi que pour l'aide pédagogique aux utilisateurs (modèle `mistral-small-latest`). Un compte gratuit suffit pour le développement.

1. Créez un compte sur [console.mistral.ai](https://console.mistral.ai)
2. Allez dans **API Keys** et cliquez sur **Create new key**
3. Copiez la clé générée — elle ne sera affichée qu'une seule fois

| Propriété | Valeur |
|-----------|--------|
| `ai.mistral.api-key` | Clé générée |

> 📖 [Guide officiel Mistral — API Keys](https://docs.mistral.ai/getting-started/quickstart/)

</details>

### 2. Fichier `.env`

Créer `.env` à la racine du projet :

```env
# Base de données
DB_NAME=votre_nom_de_db
DB_USER=postgres
DB_PASSWORD=votre_mot_de_passe
DB_PORT=5432
DB_CONTAINER_PORT=5432
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/votre_nom_de_db

# Base de données de test
TEST_DB_NAME=votre_nom_de_db_test
TEST_DB_USER=postgres
TEST_DB_PASSWORD=votre_mot_de_passe
TEST_DATASOURCE_URL=jdbc:postgresql://db-test:5432/votre_nom_de_db_test

# RabbitMQ
RABBITMQ_USER=johndoe
RABBITMQ_PASS=johndoe
RABBITMQ_PORT=5672
RABBITMQ_CONTAINER_PORT=5672
RABBITMQ_MANAGEMENT_PORT=15672
RABBITMQ_MANAGEMENT_CONTAINER_PORT=15672
```

### 3. Fichier `application-dev.properties`

Créer `src/main/resources/application-dev.properties` :

```properties
spring.application.name=Glotrush
spring.messages.basename=messages
spring.messages.encoding=UTF-8
spring.messages.fallback-to-system-locale=false
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
spring.messages.always-use-message-format=false

# JWT — générer via "openssl rand -base64 32" (voir section Services tiers › JWT & TOTP)
jwt.secret=votre_secret_jwt
jwt.access-token.expiration=900000
jwt.refresh-token.expiration=604800000

# Frontend
app.frontend.url=http://localhost:5173

# 2FA TOTP — générer via "openssl rand -base64 32" (voir section Services tiers › JWT & TOTP)
totp.encryption=votre_secret_totp

# Gmail — voir section Services tiers › Gmail (SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre.email@gmail.com
spring.mail.password=xxxx xxxx xxxx xxxx
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com
spring.mail.properties.mail.smtp.ssl.protocols=TLSv1.2
spring.mail.properties.mail.smtp.ssl.checkserveridentity=false
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

# Base de données — doit correspondre à DB_NAME, DB_USER et DB_PASSWORD du .env
spring.datasource.url=jdbc:postgresql://db:5432/votre_nom_de_db
spring.datasource.username=postgres
spring.datasource.password=votre_mot_de_passe
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.initialization-fail-timeout=60000
spring.jpa.hibernate.ddl-auto=update

# Quartz — ne pas modifier
spring.quartz.job-store-type=jdbc
spring.quartz.jdbc.initialize-schema=always
spring.quartz.properties.org.quartz.scheduler.instanceName=GlotrushScheduler
spring.quartz.properties.org.quartz.scheduler.instanceId=AUTO
spring.quartz.properties.org.quartz.jobStore.class=org.quartz.impl.jdbcjobstore.JobStoreTX
spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
spring.quartz.properties.org.quartz.jobStore.useProperties=false
spring.quartz.properties.org.quartz.jobStore.misfireThreshold=60000
spring.quartz.properties.org.quartz.jobStore.isClustered=true
spring.quartz.properties.org.quartz.jobStore.clusterCheckinInterval=10000
spring.quartz.properties.org.quartz.jobStore.tablePrefix=QRTZ_
spring.quartz.properties.org.quartz.threadPool.class=org.quartz.simpl.SimpleThreadPool
spring.quartz.properties.org.quartz.threadPool.threadCount=5
spring.quartz.properties.org.quartz.threadPool.threadPriority=5

# Actuator — ne pas modifier
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.prometheus.metrics.export.enabled=true
management.metrics.tags.application=${spring.application.name}

# Stripe — voir section Services tiers › Stripe
stripe.secretKey=sk_test_
stripe.publicKey=pk_test_
stripe.webhook-secret=whsec_
stripe.success-url=http://localhost:5173/checkout/success
stripe.cancel-url=http://localhost:5173/checkout/cancel

# Flyway — voir section Services tiers › Flyway (compte admin)
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:sql/migrations
spring.flyway.placeholder-replacement=true
spring.flyway.ignore-migration-patterns=*:missing
spring.flyway.out-of-order=true
spring.flyway.placeholders.email=votre_email_admin
spring.flyway.placeholders.first_name=votre_prenom
spring.flyway.placeholders.last_name=votre_nom
spring.flyway.placeholders.password=$2a$10$...hash_bcrypt_du_mot_de_passe...
spring.flyway.placeholders.secret_key=$2a$10$...hash_bcrypt_de_la_cle...

upload-dir=uploads/images

# Règles de leçons — ne pas modifier
glotrush.lesson.rules.xp-per-flashcard=5
glotrush.lesson.rules.seconds-per-flashcard=30
glotrush.lesson.rules.xp-per-qcm=10
glotrush.lesson.rules.seconds-per-qcm=60
glotrush.lesson.rules.matching-pair-fixed-xp=50
glotrush.lesson.rules.matching-pair-fixed-seconds=300
glotrush.lesson.rules.sorting-fixed-xp=60
glotrush.lesson.rules.sorting-fixed-seconds=360
glotrush.lesson.validation.min-items=5
glotrush.lesson.validation.max-items=20

# VAPID — voir section Services tiers › VAPID
vapid.public-key=votre_cle_publique
vapid.private-key=votre_cle_privee
vapid.subject=mailto:votre@email.com

# HuggingFace — voir section Services tiers › HuggingFace
huggingface.api-key=hf_

# Mistral AI — voir section Services tiers › Mistral AI
ai.mistral.api-key=votre_cle_mistral
ai.provider=mistral
ai.mistral.url=https://api.mistral.ai/v1/chat/completions
ai.mistral.model=mistral-small-latest
ai.quota.free-limit=5
ai.quota.premium-limit=100

# RabbitMQ — doit correspondre à RABBITMQ_USER et RABBITMQ_PASS du .env
spring.rabbitmq.host=rabbitmq
spring.rabbitmq.port=5672
spring.rabbitmq.username=votre_user_rabbitmq
spring.rabbitmq.password=votre_mot_de_passe_rabbitmq

logging.level.org.springframework.messaging=DEBUG
logging.level.org.springframework.web.socket=DEBUG

# Swagger / OpenAPI
springdoc.swagger-ui.with-credentials=true
```

### 4. Lancement

**Premier lancement**  :

```bash
docker-compose -f compose.dev.yml up -d --build
```

**Lancements suivants** :

```bash
docker-compose -f compose.dev.yml up -d
```

L'API démarre sur `http://localhost:8080`.
Les seed data sont injectées automatiquement après le démarrage de la DB.

### Commandes utiles

```bash
# Voir les logs du backend
docker-compose -f compose.dev.yml logs -f back

# Arrêter
docker-compose -f compose.dev.yml down

# Rebuild — uniquement si pom.xml modifié
docker-compose -f compose.dev.yml up -d --build back
```

## Documentation API

Documentation Swagger/OpenAPI disponible sur :

```
http://localhost:8080/swagger-ui.html
```

En développement, l'accès est ouvert. 
L'API utilise une authentification par cookie JWT (`access_token`) — le schéma `cookieAuth` est déclaré globalement dans OpenAPI.

### Routes publiques principales

| Méthode | Route | Description |
|---------|-------|-------------|
| POST | `/api/v1/auth/register` | Inscription |
| POST | `/api/v1/auth/login` | Connexion |
| POST | `/api/v1/auth/forgot-password` | Demande de reset mot de passe |
| POST | `/api/v1/auth/reset-password` | Reset mot de passe |
| POST | `/api/v1/auth/verify-2fa` | Vérification TOTP |
| GET | `/api/v1/plans/**` | Plans d'abonnement |
| POST | `/api/v1/stripe/webhook` | Webhook Stripe |
| POST | `/api/v1/contact` | Formulaire de contact |
| GET | `/actuator/health` | Santé du service |

## Tests d'intégration

Les tests d'intégration nécessitent `application-test.yml` et se lancent via Docker. Toutes les clés et credentials (Flyway, Stripe, Gmail, VAPID, HuggingFace, Mistral) peuvent être réutilisés tels quels depuis `application-dev.properties`. `jwt.secret` et `totp.encryption` peuvent avoir des valeurs indépendantes — ce sont des valeurs propres au contexte de test il faut donc en générer de nouvelles en suivant les étapes ci-desssus.

### Fichier `application-test.yml`

Créer `src/test/resources/application-test.yml` (YAML — l'indentation est obligatoire, ne pas utiliser de tabulations) :

```yaml
spring:
  main:
    allow-bean-definition-overriding: true
  datasource:
    url: jdbc:postgresql://db-test:5432/votre_nom_de_db_test
    username: postgres
    password: votre_mot_de_passe
    driver-class-name: org.postgresql.Driver

  flyway:
    baseline-on-migrate: true
    clean-disabled: false
    out-of-order: true
    locations: classpath:sql/migrations
    placeholder-replacement: true
    ignore-migration-patterns: "*:missing"
    placeholders:
      email: votre_email_admin
      first_name: votre_prenom
      last_name: votre_nom
      password: "$2a$10$...hash_bcrypt_du_mot_de_passe..."
      secret_key: "$2a$10$...hash_bcrypt_de_la_cle_secrete..."

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

  mail:
    host: smtp.gmail.com
    port: 587
    username: votre.email@gmail.com
    password: xxxx xxxx xxxx xxxx
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          ssl:
            trust: smtp.gmail.com
            protocols: TLSv1.2
            checkserveridentity: false
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000

  quartz:
    job-store-type: jdbc
    jdbc:
      initialize-schema: always
    properties:
      org:
        quartz:
          scheduler:
            instanceName: GlotrushSchedulerTest
            instanceId: AUTO
          jobStore:
            class: org.quartz.impl.jdbcjobstore.JobStoreTX
            driverDelegateClass: org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
            useProperties: false
            misfireThreshold: 60000
            isClustered: false
            tablePrefix: QRTZ_
          threadPool:
            class: org.quartz.simpl.SimpleThreadPool
            threadCount: 2
            threadPriority: 5

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    listener:
      simple:
        auto-startup: false

jwt:
  secret: votre_secret_jwt
  access-token:
    expiration: 900000
  refresh-token:
    expiration: 604800000

totp:
  encryption: votre_secret_totp

app:
  frontend:
    url: http://localhost:5173

logging:
  level:
    org:
      springframework:
        security: DEBUG
      hibernate: ERROR

stripe:
  secretKey: sk_test_
  publicKey: pk_test_
  webhookSecret: whsec_
  success-url: http://localhost:5173/payment-success
  cancel-url: http://localhost:5173/payment-cancel
```

### Lancement des tests

```bash
docker-compose -f compose.test.yml up --abort-on-container-exit
```
## Monitoring

- **Métriques** : Prometheus + Grafana
- **Erreurs & releases** : Sentry + OpenTelemetry — une release Sentry est créée automatiquement à chaque déploiement

## Sauvegardes

Politique de sauvegarde :
- Stockage sur Backblaze B2 (bucket privé, versionnement activé)

## Équipe

Projet développé par Sid-Ahmed Moussi avec Arthur Brouard et Sofiane Chadili.

---

