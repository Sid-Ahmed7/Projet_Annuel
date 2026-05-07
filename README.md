# Glotrush - Backend

Application d'apprentissage des langues structurée (Spring Boot 3.5.7).

## 🛠️ Prérequis

### Java 21 ou supérieur
Télécharger et installer depuis [adoptium.net](https://adoptium.net/)  ou [oracle.com](https://www.oracle.com/java/technologies/downloads/).

Vérifier :
```bash
java -version
```

### Maven 3.6.3+
Installer Maven globalement :

**Windows :**
1. Télécharger Maven depuis [maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
2. Extraire l'archive (ex: `C:\Program Files\Apache\maven`)
3. Ajouter `C:\Program Files\Apache\maven\bin` à la variable d'environnement `PATH`
4. Vérifier : `mvn --version`

**macOS (Homebrew) :**

Installer Homebrew si nécessaire ([brew.sh](https://brew.sh/)) :
Maven :
```bash
brew install maven
```

**Linux (apt) :**
```bash
sudo apt install maven
```

### PostgreSQL 16
Télécharger depuis [postgresql.org](https://www.postgresql.org/download/) et s'assurer que le service tourne sur le port `5432`.



### Comptes externes requis

| Service | Usage | Lien |
|---------|-------|------|
| **Gmail** | Envoi d'emails (réinitialisation mot de passe, etc.) | Compte Google → Sécurité → Mots de passe des applications |
| **Stripe** | Paiements en ligne | [dashboard.stripe.com](https://dashboard.stripe.com) → Developers → API keys |

---

## 🚀 Installation & Lancement

1. Cloner le projet.
2. Créer la base de données PostgreSQL :
   ```sql
   CREATE DATABASE glotrush;
   ```
3. Créer le fichier de configuration :
   ```bash
   cp src/main/resources/application.properties src/main/resources/application-dev.properties
   ```
4. Remplir `src/main/resources/application-dev.properties` avec les valeurs suivantes :

```properties
# JWT - générer avec : openssl rand -base64 32
jwt.secret=

# URL du frontend
app.frontend.url=http://localhost:5173

# TOTP (2FA) - générer avec : openssl rand -base64 32
totp.encryption=

# Gmail - utiliser un mot de passe d'application (compte Google → Sécurité → Mots de passe des applications)
spring.mail.username=
spring.mail.password=

# Base de données
spring.datasource.url=jdbc:postgresql://localhost:5432/glotrush
spring.datasource.username=
spring.datasource.password=

# Stripe - dashboard.stripe.com → Developers → API keys
# secretKey : sk_test_...  |  publicKey : pk_test_...
# webhook-secret : Developers → Webhooks → whsec_...
stripe.secretKey=
stripe.publicKey=
stripe.webhook-secret=
stripe.success-url=http://localhost:5173/payment/success
stripe.cancel-url=http://localhost:5173/payment/cancel

# Compte admin créé au démarrage par Flyway (valeurs directes, pas de variables d'env)
# password et secret_key : bcrypt hash
spring.flyway.placeholders.email=
spring.flyway.placeholders.first_name=
spring.flyway.placeholders.last_name=
spring.flyway.placeholders.password=
spring.flyway.placeholders.secret_key=
```

5. Lancer l'application avec le profil dev :
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

## 📊 Base de données

- **Migrations (Flyway) :** Toujours utiliser des conditions (`IF EXISTS`, `IF NOT EXISTS`, blocs `DO $$`) dans les scripts SQL pour garantir l'idempotence et la stabilité des tests d'intégration.
- **Données de test :** Pour peupler la base avec des données cohérentes :
```powershell
psql -U postgres -d glotrush -f src/main/resources/sql/seed_data.sql
```

## 📖 Documentation

Pour générer la Javadoc du projet :
```bash
./mvnw javadoc:javadoc
```
La documentation sera générée dans `target/site/apidocs/`.

## 🚢 Déploiement

### Docker

#### Build l'image
```bash
docker build -t glotrush-back:latest .
```

#### Tag et Push
```bash
docker tag glotrush-back:latest arthurbrd/glotrush-back:latest
docker push arthurbrd/glotrush-back:latest
```

```bash
# Lancer tous les services (DB + Back)
docker-compose up -d

# Voir les logs
docker-compose logs -f back
```
