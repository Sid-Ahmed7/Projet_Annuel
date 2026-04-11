# Guide de Développement - Glotrush

## 🛠 Stack Technique
- **Langage :** Java 21
- **Framework :** Spring Boot 3.5.7
- **Base de données :** PostgreSQL
- **Migration DB :** Flyway
- **Mapping :** MapStruct 1.6.3
- **Sécurité :** Spring Security, OAuth2 (Client & Resource Server), JWT (jjwt 0.13.0), TOTP (2FA)
- **Paiements :** Stripe (stripe-java 28.2.0)
- **Utilitaire :** Lombok
- **Validation :** Jakarta Validation
- **Planification :** Quartz Scheduler

## 📖 Récapitulatif Fonctionnel
Glotrush est une application d'apprentissage des langues structurée autour des axes suivants :

1.  **Gestion des Comptes & Authentification :**
    *   Inscription/Connexion avec support du 2FA (TOTP).
    *   Gestion des rôles (USER, ADMIN).
    *   Réinitialisation de mot de passe et gestion des sessions via Refresh Tokens.
2.  **Apprentissage & Contenu :**
    *   **Langues :** Gestion de différentes langues cibles.
    *   **Thématiques (Topics) :** Les cours sont organisés par thèmes (ex: Famille, Voyage) avec des niveaux de difficulté.
    *   **Leçons :** Différents types de leçons (QCM, Flashcards, Matching Pairs, Exercices de tri).
    *   **Progression :** Suivi des points d'expérience (XP), niveaux de maîtrise et historique des leçons complétées.
3.  **Monétisation :**
    *   Système d'abonnement (Plans, Subscriptions).
    *   Intégration Stripe pour les paiements et historique des transactions.
4.  **Profil Utilisateur :**
    *   Personnalisation du profil (avatar, bio, préférences).
    *   Suivi des langues apprises par l'utilisateur.

## 📏 Principes de Rédaction du Code pour Junie

Afin de maintenir la cohérence et la qualité du projet, les principes suivants doivent être respectés par Junie :

1.  **Architecture en Couches :**
    *   Respecter strictement la séparation : `Controller` -> `Service` (via interface) -> `Repository`.
    *   Utiliser les DTO pour les échanges de données entre les couches externes et les services.
2.  **Utilisation de Lombok :**
    *   Utiliser `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` pour les entités et DTO.
    *   Privilégier `@RequiredArgsConstructor` pour l'injection de dépendances par constructeur dans les contrôleurs et services.
3.  **Mapping avec MapStruct :**
    *   Toujours utiliser MapStruct pour transformer les entités en DTO et inversement. Ne pas écrire de code de conversion manuel si un mapper peut être utilisé.
4.  **Gestion des Entités :**
    *   Utiliser des UUID pour les clés primaires (`GenerationType.UUID`).
    *   Utiliser `@CreationTimestamp` et `@UpdateTimestamp` (Hibernate) ou `@PrePersist`/`@PreUpdate` (JPA) pour les dates de création/modification.
    *   Privilégier le Lazy Loading pour les relations OneToOne/ManyToOne sauf justification explicite.
5.  **Internationalisation & Messages :**
    *   Utiliser `MessageSource` pour tous les messages de retour utilisateur.
    *   Passer par `LocaleUtils.getCurrentLocale()` pour récupérer la locale courante.
6.  **Sécurité :**
    *   Vérifier systématiquement les autorisations dans les services ou via les annotations de sécurité.
    *   Utiliser les classes utilitaires existantes pour extraire les informations de l'utilisateur authentifié.
7.  **Qualité & Style :**
    *   Suivre la convention de nommage CamelCase (PascalCase pour les classes, camelCase pour les méthodes/variables).
    *   Les noms de tables et colonnes dans les entités doivent être en snake_case via `@Table` et `@Column`.
    *   Préférer les interfaces pour les services (ex: `IAuthService` et `AuthService`).
