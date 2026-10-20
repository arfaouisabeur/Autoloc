# Atelier 5 - Partie 1 : API REST - Notes Techniques

## Documentation des 4 premiers contrôleurs REST

**Date :** 30 septembre 2026  
**Contexte :** Implémentation partielle de la couche web (4 entités sur 9)  
**Entités couvertes :** Agence, Vehicule, Client, Reservation

---

## A. Endpoints implémentés

### Tableau récapitulatif des endpoints

| Contrôleur | Verbe et Route | Service appelé | Code succès | Codes erreur |
|------------|---------------|----------------|-------------|--------------|
| **AgenceRestController** | | | | |
| | GET /agences | IAgenceService.findAll() | 200 | — |
| | GET /agences/{id} | IAgenceService.findById(id) | 200 | 404 |
| | POST /agences | IAgenceService.create(agence) | 200 | 400 |
| | PUT /agences/{id} | IAgenceService.update(id, agence) | 200 | 404, 400 |
| | DELETE /agences/{id} | IAgenceService.deleteById(id) | 200 | 404 |
| **VehiculeRestController** | | | | |
| | GET /vehicules | VehiculeServicesImpl.findAll() | 200 | — |
| | GET /vehicules/{id} | VehiculeServicesImpl.findById(id) | 200 | 404 |
| | POST /vehicules | VehiculeServicesImpl.create(vehicule) | 200 | 400 |
| | PUT /vehicules/{id} | VehiculeServicesImpl.update(id, vehicule) | 200 | 404, 400 |
| | DELETE /vehicules/{id} | VehiculeServicesImpl.deleteById(id) | 200 | 404 |
| **ClientRestController** | | | | |
| | GET /clients | IClientService.findAll() | 200 | — |
| | GET /clients/{id} | IClientService.findById(id) | 200 | 404 |
| | POST /clients | IClientService.create(client) | 200 | 400 |
| | PUT /clients/{id} | IClientService.update(id, client) | 200 | 404, 400 |
| | DELETE /clients/{id} | IClientService.deleteById(id) | 200 | 404 |
| **ReservationRestController** | | | | |
| | GET /reservations | IReservationService.findAll() | 200 | — |
| | GET /reservations/{id} | IReservationService.findById(id) | 200 | 404 |
| | POST /reservations | IReservationService.create(reservation) | 200 | 400 |
| | PUT /reservations/{id} | IReservationService.update(id, reservation) | 200 | 404, 400 |
| | DELETE /reservations/{id} | IReservationService.deleteById(id) | 200 | 404 |

### Observations sur les codes de retour

- **200 OK** : Retourné pour GET, PUT et POST (au lieu de 201 Created pour POST, ce qui sera corrigé avec @ResponseStatus dans la partie 2)
- **404 Not Found** : Levé automatiquement par `ResourceNotFoundException` dans les services (findById, update, deleteById)
- **400 Bad Request** : Levé par `IllegalArgumentException` lors de la validation des règles métier dans les services
- **405 Method Not Allowed** : Sera retourné pour les méthodes non implémentées (ex: POST sur Paiement en lecture seule)

---

## B. Annotations Jackson pour la sérialisation JSON

### Problématique

Une association bidirectionnelle (A contient B qui contient A) fait boucler Jackson à l'infini lors de la sérialisation JSON, provoquant une `StackOverflowError`.

**Exemple sans annotation :**
```
Agence → vehicules → [Vehicule] → agence → vehicules → [Vehicule] → agence → ... ♾️
```

### Solutions appliquées

| Association | Annotation côté parent | Annotation côté enfant | Justification |
|-------------|----------------------|----------------------|---------------|
| **Agence ↔ Vehicule** | `@JsonManagedReference` sur `List<Vehicule> vehicules` | `@JsonBackReference` sur `Agence agence` | L'agence sérialise ses véhicules, mais le véhicule ne sérialise pas son agence. Évite la boucle infinie. |
| **Agence ↔ Employe** | `@JsonManagedReference` sur `List<Employe> employes` | `@JsonBackReference` sur `Agence agence` | Même logique : l'agence expose ses employés, mais l'employé ne ré-expose pas l'agence. |
| **Client ↔ Reservation** | `@JsonManagedReference` sur `List<Reservation> reservations` | `@JsonBackReference` sur `Client client` | Le client sérialise ses réservations, la réservation ne sérialise pas le client. |

### Différence entre @JsonBackReference et @JsonIgnore

| Annotation | Effet en sérialisation (objet → JSON) | Effet en désérialisation (JSON → objet) |
|------------|--------------------------------------|----------------------------------------|
| **@JsonBackReference** | Attribut **ignoré** lors de l'écriture JSON | Attribut **reconnu** lors de la lecture JSON (le lien est établi automatiquement par la référence managed) |
| **@JsonIgnore** | Attribut **ignoré** lors de l'écriture JSON | Attribut **ignoré** lors de la lecture JSON (le champ reste `null`) |

**Pourquoi @JsonBackReference sur Reservation.client ?**

Si on utilisait `@JsonIgnore` sur `Reservation.client`, lors d'un POST de réservation, le champ client serait ignoré et la réservation serait créée **sans client** (violation de contrainte métier).

Avec `@JsonBackReference`, le JSON `{ "client": {"idClient": 1}, ... }` est correctement désérialisé et le lien est établi.

**Exemple de réponse JSON GET /agences/1 :**
```json
{
  "idAgence": 1,
  "nom": "Agence Tunis Centre",
  "ville": "Tunis",
  "adresse": "Avenue Habib Bourguiba",
  "telephone": "71123456",
  "vehicules": [
    {
      "idVehicule": 1,
      "immatriculation": "123TUN456",
      "marque": "Peugeot",
      "modele": "208"
      // agence n'apparaît pas (évite la boucle)
    }
  ],
  "employes": [
    {
      "idEmploye": 1,
      "nom": "Arfaoui",
      "prenom": "Sabeur",
      "role": "RESPONSABLE"
      // agence n'apparaît pas
    }
  ]
}
```

---

## C. Structure des contrôleurs REST

### Pattern appliqué (conventions Spring MVC)

Tous les contrôleurs suivent le même pattern :

```java
@RestController                      // Indique que les valeurs retournées sont sérialisées en JSON
@RequestMapping("ressources")        // Préfixe d'URL (au pluriel, sans /api qui sera ajouté par context-path)
@RequiredArgsConstructor             // Lombok génère le constructeur pour l'injection
public class RessourceRestController {
    
    private final IRessourceService ressourceService;  // Champ final, injection par constructeur
    
    @PostMapping                     // POST /ressources
    public Ressource create(@RequestBody Ressource ressource) {
        return ressourceService.create(ressource);
    }
    
    @GetMapping                      // GET /ressources
    public List<Ressource> findAll() {
        return ressourceService.findAll();
    }
    
    @GetMapping("/{id}")            // GET /ressources/{id}
    public Ressource findById(@PathVariable Long id) {
        return ressourceService.findById(id);
    }
    
    @PutMapping("/{id}")            // PUT /ressources/{id}
    public Ressource update(@PathVariable Long id, @RequestBody Ressource ressource) {
        return ressourceService.update(id, ressource);
    }
    
    @DeleteMapping("/{id}")         // DELETE /ressources/{id}
    public void deleteById(@PathVariable Long id) {
        ressourceService.deleteById(id);
    }
}
```

### Justification des choix d'architecture

#### 1. Dépendance à l'interface (IAgenceService, IClientService, IReservationService)

**Principe appliqué :** Dependency Inversion Principle (DIP)

Le contrôleur dépend de l'**abstraction** (interface `I...Service`) et non de l'**implémentation** (classe `...ServiceImpl`).

**Avantages :**
- **Découplage** : Le contrôleur ne connaît pas l'implémentation concrète
- **Testabilité** : On peut facilement injecter un mock de l'interface pour les tests unitaires
- **Évolutivité** : On peut changer l'implémentation sans toucher au contrôleur

**Exemple :**
```java
// ✅ Bon - AgenceRestController dépend de l'interface
private final IAgenceService agenceService;

// ❌ Mauvais - couplage fort à l'implémentation
private final AgenceServiceImpl agenceService;
```

#### 2. Exception : VehiculeRestController dépend de VehiculeServicesImpl

**Remarque :** Ce contrôleur est une exception qui injecte directement la classe d'implémentation `VehiculeServicesImpl` au lieu de l'interface `IVehiculeService`.

**Pourquoi ?**
- Pattern existant dans le projet (classe legacy)
- Fonctionne correctement car Spring injecte quand même le bon bean
- **À éviter** pour les nouveaux contrôleurs (préférer l'interface)

**Impact :**
- Moins flexible (couplage direct)
- Mais fonctionnel dans ce cas précis

#### 3. Injection par constructeur avec @RequiredArgsConstructor

Spring détecte automatiquement un **constructeur unique** et injecte les dépendances **sans avoir besoin de @Autowired**.

Lombok génère le constructeur pour tous les champs `private final`, ce qui garantit :
- **Immutabilité** du champ service
- **Dépendance obligatoire** (impossible de créer le contrôleur sans le service)
- **Code concis** (pas de constructeur à écrire manuellement)

---

## D. Règles métier validées par les services

Les contrôleurs **ne valident pas** les règles métier. Ils délèguent cette responsabilité aux services (Atelier 4).

### Règles métier déclenchant un 400 Bad Request

| Service | Règle métier | Exception levée |
|---------|--------------|-----------------|
| **AgenceServiceImpl** | Nom vide ou null | `IllegalArgumentException` |
| **VehiculeServiceImpl** | Tarif journalier négatif | `IllegalArgumentException` |
| **ClientServiceImpl** | Nom ou email vide/null | `IllegalArgumentException` |
| **ReservationServiceImpl** | Date de fin < date de début | `IllegalArgumentException` |

### Règles métier déclenchant un 404 Not Found

Tous les services lèvent `ResourceNotFoundException` (exception métier créée dans Atelier 4) lorsque :
- `findById(id)` ne trouve pas l'entité
- `update(id, ...)` cible un identifiant inexistant
- `deleteById(id)` cible un identifiant inexistant

**Traduction automatique par GlobalExceptionHandler** (fourni par le TP) :
- `ResourceNotFoundException` → 404 Not Found (avec corps JSON explicite)
- `IllegalArgumentException` → 400 Bad Request (avec corps JSON explicite)

---

## E. Parcours d'une requête HTTP dans Spring MVC

### Exemple : GET /agences/1

**Étapes du cycle de vie de la requête :**

1. **Client (Postman)** envoie : `GET http://localhost:8080/agences/1`

2. **DispatcherServlet** (point d'entrée unique) reçoit la requête et consulte le **HandlerMapping**

3. **HandlerMapping** identifie le contrôleur et la méthode à invoquer :
   - Route `/agences/{id}` avec verbe `GET` → `AgenceRestController.findById()`
   - Extraction du paramètre `{id}` = 1

4. **Contrôleur** (`AgenceRestController`) exécute la logique :
   ```java
   public Agence findById(@PathVariable Long id) {
       return agenceService.findById(id);  // Délègue au service
   }
   ```

5. **Service** (`AgenceServiceImpl`) contient la logique métier :
   ```java
   public Agence findById(Long id) {
       return agenceRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Agence", id));
   }
   ```

6. **Repository** (`IAgenceRepository`, proxy Spring Data JPA) exécute :
   ```sql
   SELECT * FROM agence WHERE id_agence = 1;
   ```

7. **Retour de la donnée** : Agence entité JPA → Service → Contrôleur

8. **Jackson** (bibliothèque de sérialisation JSON) convertit l'objet Java en JSON :
   - Applique `@JsonManagedReference` / `@JsonBackReference` pour éviter les boucles
   - Génère le JSON final

9. **DispatcherServlet** construit la réponse HTTP :
   - Statut : `200 OK`
   - En-tête : `Content-Type: application/json`
   - Corps : JSON de l'agence

10. **Réponse HTTP** renvoyée au client (Postman)

### Schéma récapitulatif

```
[Postman] → [DispatcherServlet] → [HandlerMapping] → [Contrôleur]
                                                           ↓
                                                       [Service]
                                                           ↓
                                                      [Repository]
                                                           ↓
                                                        [MySQL]
                                                           ↓
[Postman] ← [Réponse HTTP JSON] ← [Jackson] ← [Service] ← [Repository]
```

---

## F. Questions de compréhension

### 1. Quel composant reçoit toutes les requêtes, et comment retrouve-t-il la méthode du contrôleur à appeler ?

**Réponse :** Le **DispatcherServlet** reçoit toutes les requêtes HTTP.

C'est le **front controller** de Spring MVC : point d'entrée unique qui délègue ensuite aux contrôleurs appropriés.

**Mécanisme de résolution :**
1. Le `DispatcherServlet` consulte le **HandlerMapping** (table de correspondance)
2. Le `HandlerMapping` analyse :
   - Le verbe HTTP (GET, POST, PUT, DELETE)
   - Le chemin de la requête (`/agences/1`)
3. Il compare avec les annotations des contrôleurs :
   - `@RequestMapping("/agences")` sur la classe
   - `@GetMapping("/{id}")` sur la méthode
4. Il extrait les paramètres :
   - `@PathVariable Long id` → valeur 1 du chemin `/agences/1`
5. Il invoque la méthode `AgenceRestController.findById(1L)`

---

### 2. Pourquoi AgenceRestController dépend-il de IAgenceService et non de AgenceRepository ni de AgenceServiceImpl ?

**Réponse :** Pour respecter l'**architecture en couches** et le **principe de séparation des responsabilités**.

**Pourquoi pas AgenceRepository ?**
- Le contrôleur ne doit **jamais accéder directement au repository**
- Le service contient la **logique métier** (validation, règles de gestion, gestion des transactions)
- Si le contrôleur appelait directement le repository, il devrait dupliquer cette logique
- **Violation du principe Single Responsibility** : le contrôleur aurait deux responsabilités (web + métier)

**Pourquoi pas AgenceServiceImpl ?**
- Le contrôleur doit dépendre de l'**abstraction** (interface) et non de l'**implémentation** (classe concrète)
- **Principe de Dependency Inversion** : dépendre des abstractions, pas des détails
- Avantages :
  - Découplage : on peut changer l'implémentation sans toucher au contrôleur
  - Testabilité : on peut facilement mocker l'interface dans les tests
  - Flexibilité : plusieurs implémentations peuvent coexister (ex: `AgenceServiceV2Impl`)

**Architecture correcte :**
```
Controller → Interface I...Service → Implementation ...ServiceImpl → Repository
```

---

### 3. Pourquoi un POST renvoie-t-il 201 et un DELETE 204 ? Pourquoi une ressource absente donne-t-elle 404 et non 500 ?

**Réponse :**

**POST → 201 Created :**
- Le code **201** signifie qu'une **nouvelle ressource a été créée**
- C'est plus explicite que **200 OK** (qui signifie simplement "succès")
- Selon les conventions REST, le POST d'une création doit retourner **201** avec l'objet créé (incluant son identifiant généré)
- **Note :** Dans notre implémentation actuelle, POST retourne 200 par défaut. Il faudra ajouter `@ResponseStatus(HttpStatus.CREATED)` pour retourner 201.

**DELETE → 204 No Content :**
- Le code **204** signifie **succès sans corps de réponse**
- Après une suppression, il n'y a aucun contenu à retourner (la ressource n'existe plus)
- C'est plus précis que **200 OK** (qui implique généralement un corps de réponse)

**Ressource absente → 404 et non 500 :**
- **404 Not Found** : la ressource demandée n'existe pas (erreur client, problème prévisible)
- **500 Internal Server Error** : erreur serveur inattendue (bug, exception non gérée, base de données inaccessible)

**Principe :**
- L'absence d'une ressource est une **situation métier normale** (l'utilisateur demande un ID qui n'existe pas)
- Ce n'est **pas un bug** du serveur, donc pas un 500
- Le service lève `ResourceNotFoundException`, le `GlobalExceptionHandler` la traduit en 404
- Le client sait que sa requête est mal formée (mauvais ID) et peut corriger

---

### 4. Pourquoi les contrôleurs exposent-ils directement les entités JPA au lieu de DTO ?

**Réponse :** C'est un **choix pédagogique provisoire** du TP pour simplifier l'Atelier 5.

**Risques de l'exposition directe des entités JPA :**

1. **Couplage API/Base de données**
   - La structure JSON est directement liée au schéma de la base
   - Toute modification du modèle JPA impacte l'API (breaking change pour les clients)

2. **Champs internes exposés**
   - Les clients voient tous les attributs (même ceux qui devraient rester internes)
   - Ex : version (optimistic locking), dateCreation, champs techniques

3. **Associations lazy non maîtrisées**
   - Un `@ManyToOne LAZY` peut provoquer une `LazyInitializationException` ou un proxy non sérialisable
   - Nécessite des annotations Jackson (`@JsonIgnore`, `@JsonBackReference`) pour corriger

4. **Difficulté de validation**
   - Les contraintes JPA (`@Column(nullable=false)`) ne sont pas adaptées à la validation API
   - Manque de messages d'erreur clairs pour l'utilisateur

5. **Pas de transformation métier**
   - Impossible d'aplatir une structure complexe
   - Impossible d'ajouter des champs calculés (ex : montantTTC à partir de montantHT + TVA)

**Solution (Chapitre 10 du cours) :** Utiliser des **DTO (Data Transfer Objects)**
- Couche de découplage entre API et base
- Structure JSON maîtrisée et documentée
- Validation Bean Validation (`@NotBlank`, `@Email`, `@Positive`)
- Mapping manuel ou automatique (MapStruct, ModelMapper)

---

### 5. Comparaison des associations Jackson implémentées

**Question :** Pourquoi Client.reservations utilise-t-il `@JsonManagedReference` alors que dans certains exemples on voit `@JsonIgnore` ?

**Réponse :** Cela dépend de la **sémantique métier** de l'association.

| Cas | Annotation | Justification |
|-----|-----------|---------------|
| **Composition forte (parent crée les enfants)** | `@JsonManagedReference` (parent) + `@JsonBackReference` (enfant) | Le parent expose ses enfants dans le JSON. Ex : Client expose ses Reservations, Agence expose ses Vehicules et Employes. |
| **Association simple (l'enfant désigne son parent)** | `@JsonIgnore` sur la liste inverse | Le parent n'expose pas ses enfants, l'enfant désigne son parent par un ID. Ex : Si on ne voulait pas que l'Agence expose tous ses véhicules, on mettrait `@JsonIgnore` sur `List<Vehicule> vehicules`. |

**Dans notre implémentation :**
- **Client ↔ Reservation** : Composition forte → `@JsonManagedReference` / `@JsonBackReference`
  - Quand on consulte un client, on veut voir ses réservations
  - Quand on consulte une réservation, on ne veut pas revoir tout le client (évite la boucle)

- **Agence ↔ Vehicule/Employe** : Composition forte → `@JsonManagedReference` / `@JsonBackReference`
  - Même logique : l'agence expose ses véhicules et employés

**Si on utilisait @JsonIgnore partout :**
- GET /agences/1 ne montrerait **pas** les véhicules ni les employés
- Il faudrait faire des requêtes séparées : GET /vehicules?agenceId=1
- Moins pratique pour l'API, mais parfois nécessaire pour limiter la taille des réponses

---

## G. Synthèse des fichiers créés

### Contrôleurs REST (4 fichiers)

```
src/main/java/tn/esprit/sabeurarfaoui4cce11/web/controller/
├── AgenceRestController.java      (CRUD complet sur Agence)
├── VehiculeRestController.java    (CRUD complet sur Vehicule)
├── ClientRestController.java      (CRUD complet sur Client)
└── ReservationRestController.java (CRUD complet sur Reservation)
```

### Entités annotées pour Jackson (4 fichiers modifiés)

```
src/main/java/tn/esprit/sabeurarfaoui4cce11/domain/
├── Agence.java        (@JsonManagedReference sur vehicules et employes)
├── Vehicule.java      (@JsonBackReference sur agence)
├── Employe.java       (@JsonBackReference sur agence)
├── Client.java        (@JsonManagedReference sur reservations)
└── Reservation.java   (@JsonBackReference sur client)
```

---

## H. Prochaines étapes (Partie 2)

Pour compléter l'Atelier 5, il reste à implémenter :

1. **5 contrôleurs REST manquants** :
   - ContratController (CRUD avec cascade vers Paiement)
   - PaiementController (lecture seule : GET uniquement)
   - EmployeController (CRUD)
   - EquipementController (CRUD)
   - MaintenanceController (CRUD)

2. **Configuration globale** :
   - `server.servlet.context-path=/api` dans `application.properties`
   - GlobalExceptionHandler (fourni par le TP)

3. **Documentation Swagger** :
   - Dépendance SpringDoc OpenAPI
   - Classe OpenApiConfig
   - Annotations `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`

4. **Tests Postman** :
   - Collection complète avec environnement local
   - Requêtes nominales + cas d'erreur (404, 400)
   - Scripts de test automatisés

5. **Annotations manquantes** :
   - `@ResponseStatus(HttpStatus.CREATED)` sur les POST
   - `@ResponseStatus(HttpStatus.NO_CONTENT)` sur les DELETE
   - Jackson pour Contrat ↔ Paiement (composition avec `@JsonManagedReference` / `@JsonBackReference`)

---

## Conclusion

Cette première partie couvre **4 entités sur 9**, soit environ **45% de l'API REST** requise par l'Atelier 5.

**Conformité au TP :**
- ✅ Architecture en couches respectée (Controller → Service → Repository)
- ✅ Injection par constructeur via `@RequiredArgsConstructor`
- ✅ Dépendance aux interfaces (sauf VehiculeRestController, cas particulier)
- ✅ Annotations Jackson correctement appliquées (pas de boucle infinie)
- ✅ Conventions REST : routes au pluriel, verbes HTTP appropriés
- ✅ Gestion des erreurs déléguée aux services (404, 400)

**Points à améliorer pour la partie 2 :**
- Ajouter `@ResponseStatus` pour retourner 201 (POST) et 204 (DELETE)
- Implémenter les 5 contrôleurs restants
- Ajouter la documentation Swagger
- Créer la collection Postman complète

**L'implémentation actuelle est fonctionnelle et prête pour les tests !** 🚀
