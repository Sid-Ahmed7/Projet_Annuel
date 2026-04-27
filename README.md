# Glotrush - Backend

Application d'apprentissage des langues structurée (Spring Boot 3.5.7).

## 🚀 Installation & Lancement

1. Cloner le projet.
2. Configurer la base de données PostgreSQL dans `application-dev.properties`.
3. Lancer l'application :
   ```bash
   ./mvnw spring-boot:run
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
