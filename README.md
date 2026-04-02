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

Pour peupler la base de données avec des données de test cohérentes :
```powershell
psql -U postgres -d glotrush -f src/main/resources/sql/seed_data.sql
```

## 📖 Documentation

Pour générer la Javadoc du projet :
```bash
./mvnw javadoc:javadoc
```
La documentation sera générée dans `target/site/apidocs/`.
