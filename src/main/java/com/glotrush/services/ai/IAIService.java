package com.glotrush.services.ai;

public interface IAIService {
    /**
     * Génère un contenu JSON à partir d'un prompt et le désérialise dans la classe spécifiée.
     *
     * @param prompt        Le prompt envoyé à l'IA.
     * @param responseClass La classe cible pour la désérialisation.
     * @param <T>           Le type de l'objet de retour.
     * @return L'objet désérialisé.
     */
    <T> T generateJsonContent(String prompt, Class<T> responseClass);

    /**
     * Génère un contenu textuel brut à partir d'un prompt.
     *
     * @param prompt Le prompt envoyé à l'IA.
     * @return La réponse brute de l'IA.
     */
    String generateRawContent(String prompt);
}
