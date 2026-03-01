# Glotrush - Application d'apprentissage de langues

Application Spring Boot pour apprendre des langues développée avec Java 21.

## Prérequis

Avant de lancer le projet, assurez-vous d'avoir installé :

- **Java 21** (JDK 21)
- **Maven 3.6+** (ou utilisez le wrapper Maven inclus : `mvnw` / `mvnw.cmd`)
- **PostgreSQL** (version 12 ou supérieure)
- **Git**

## Configuration

### 1. Base de données PostgreSQL

Créez une base de données PostgreSQL pour le projet :

```sql
CREATE DATABASE glotrush;
```

### 2. Fichier de configuration

Le projet utilise le profil `dev` par défaut. Vous devez créer un fichier `src/main/resources/application-dev.properties` avec la configuration suivante :

```properties
# Configuration de la base de données
spring.datasource.url=jdbc:postgresql://localhost:5432/glotrush
spring.datasource.username=votre_username
spring.datasource.password=votre_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Configuration JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Port du serveur
server.port=8080

# Configuration JWT
jwt.secret=votre_secret_jwt_assez_long_et_securise_minimum_256_bits
jwt.access-token.expiration=3600000
jwt.refresh-token.expiration=86400000

# Configuration TOTP (Two-Factor Authentication)
totp.encryption=votre_cle_encryption_totp

# Configuration Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre_email@gmail.com
spring.mail.password=votre_mot_de_passe_app
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# URL du frontend
app.frontend.url=http://localhost:3000
```

**Note importante** : Le fichier `application-dev.properties` est ignoré par Git (dans `.gitignore`) pour des raisons de sécurité. Ne commitez jamais ce fichier avec des informations sensibles.

## Installation et lancement

### Option 1 : Utiliser Maven Wrapper (recommandé)

Sur **Windows** :
```bash
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

Sur **Linux/Mac** :
```bash
./mvnw clean install
./mvnw spring-boot:run
```

### Option 2 : Utiliser Maven installé localement

```bash
mvn clean install
mvn spring-boot:run
```

### Option 3 : Exécuter depuis votre IDE

1. Ouvrez le projet dans votre IDE (IntelliJ IDEA, Eclipse, VS Code, etc.)
2. Assurez-vous que Java 21 est configuré comme JDK
3. Exécutez la classe principale : `com.glotrush.GlotrushApplication`

## Vérification

Une fois l'application lancée, vous pouvez vérifier qu'elle fonctionne en accédant à :

- **Application** : http://localhost:8080
- **Actuator Health** : http://localhost:8080/actuator/health
- **Prometheus Metrics** : http://localhost:8080/actuator/prometheus

## Tests

Pour exécuter les tests unitaires :

```bash
# Avec Maven Wrapper
.\mvnw.cmd test

# Avec Maven
mvn test
```

Les tests utilisent le profil `test` et nécessitent une configuration dans `src/test/resources/application-test.yml` (non versionné).

## Structure du projet

```
Projet_Annuel/
├── src/
│   ├── main/
│   │   ├── java/com/glotrush/
│   │   │   ├── config/          # Configurations Spring
│   │   │   ├── controllers/     # Contrôleurs REST
│   │   │   ├── services/        # Services métier
│   │   │   ├── repositories/    # Repositories JPA
│   │   │   ├── entities/        # Entités JPA
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── security/        # Configuration sécurité
│   │   │   └── GlotrushApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── messages*.properties
│   └── test/
│       └── java/com/glotrush/   # Tests unitaires et d'intégration
├── pom.xml
└── README.md
```

## Technologies utilisées

- **Spring Boot 3.5.7**
- **Java 21**
- **PostgreSQL** (base de données)
- **Spring Security** (authentification et autorisation)
- **JWT** (JSON Web Tokens)
- **TOTP** (Two-Factor Authentication)
- **Spring Data JPA** (persistance)
- **WebSocket** (communication temps réel)
- **Spring Mail** (envoi d'emails)
- **Lombok** (réduction du code boilerplate)
- **Maven** (gestion des dépendances)

## Dépannage

### Erreur de connexion à la base de données

Vérifiez que :
- PostgreSQL est démarré
- Les identifiants dans `application-dev.properties` sont corrects
- La base de données `glotrush` existe

### Erreur de port déjà utilisé

Changez le port dans `application-dev.properties` :
```properties
server.port=8081
```

### Problème avec Java 21

Vérifiez votre version Java :
```bash
java -version
```

Vous devez avoir Java 21 installé.

## Contribution

Ce projet fait partie d'un projet annuel. Pour contribuer :

1. Créez une branche depuis `develop`
2. Effectuez vos modifications
3. Créez une pull request vers `develop`

## Licence

[À compléter]
