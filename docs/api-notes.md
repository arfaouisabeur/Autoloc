# Atelier 5 : API REST - Notes Techniques

## A. Endpoints

| Contrôleur | Verbe et route | Service appelé | Succès | Erreurs |
|------------|---------------|----------------|--------|---------|
| **ContratController** | GET /api/contrats | findAll() | 200 | — |
| | GET /api/contrats/{id} | findById(id) | 200 | 404 |
| | POST /api/contrats | create(contrat) | 201 | 400 |
| | PUT /api/contrats/{id} | update(id, contrat) | 200 | 404, 400 |
| | DELETE /api/contrats/{id} | deleteById(id) | 204 | 404 |
| **PaiementController** | GET /api/paiements | findAll() | 200 | — |
| | GET /api/paiements/{id} | findById(id) | 200 | 404 |
| **AgenceController** | GET /api/agences | findAll() | 200 | — |
| | GET /api/agences/{id} | findById(id) | 200 | 404 |
| | POST /api/agences | create(agence) | 201 | 400 |
| | PUT /api/agences/{id} | update(id, agence) | 200 | 404, 400 |
| | DELETE /api/agences/{id} | deleteById(id) | 204 | 404 |
| **EmployeController** | GET /api/employes | findAll() | 200 | — |
| | GET /api/employes/{id} | findById(id) | 200 | 404 |
| | POST /api/employes | create(employe) | 201 | 400 |
| | PUT /api/employes/{id} | update(id, employe) | 200 | 404, 400 |
| | DELETE /api/employes/{id} | deleteById(id) | 204 | 404 |
| **VehiculeController** | GET /api/vehicules | findAll() | 200 | — |
| | GET /api/vehicules/{id} | findById(id) | 200 | 404 |
| | POST /api/vehicules | create(vehicule) | 201 | 400 |
| | PUT /api/vehicules/{id} | update(id, vehicule) | 200 | 404, 400 |
| | DELETE /api/vehicules/{id} | deleteById(id) | 204 | 404 |
| **EquipementController** | GET /api/equipements | findAll() | 200 | — |
| | GET /api/equipements/{id} | findById(id) | 200 | 404 |
| | POST /api/equipements | create(equipement) | 201 | 400 |
| | PUT /api/equipements/{id} | update(id, equipement) | 200 | 404, 400 |
| | DELETE /api/equipements/{id} | deleteById(id) | 204 | 404 |
| **ClientController** | GET /api/clients | findAll() | 200 | — |
| | GET /api/clients/{id} | findById(id) | 200 | 404 |
| | POST /api/clients | create(client) | 201 | 400 |
| | PUT /api/clients/{id} | update(id, client) | 200 | 404, 400 |
| | DELETE /api/clients/{id} | deleteById(id) | 204 | 404 |
| **ReservationController** | GET /api/reservations | findAll() | 200 | — |
| | GET /api/reservations/{id} | findById(id) | 200 | 404 |
| | POST /api/reservations | create(reservation) | 201 | 400 |
| | PUT /api/reservations/{id} | update(id, reservation) | 200 | 404, 400 |
| | DELETE /api/reservations/{id} | deleteById(id) | 204 | 404 |
| **MaintenanceController** | GET /api/maintenances | findAll() | 200 | — |
| | GET /api/maintenances/{id} | findById(id) | 200 | 404 |
| | POST /api/maintenances | create(maintenance) | 201 | 400 |
| | PUT /api/maintenances/{id} | update(id, maintenance) | 200 | 404, 400 |
| | DELETE /api/maintenances/{id} | deleteById(id) | 204 | 404 |

---

## B. Annotations Jackson

### Associations annotées

| Association | Annotation retenue | Justification |
|-------------|-------------------|---------------|
| **Contrat ↔ Paiement** | `@JsonManagedReference("contrat-paiements")` sur Contrat.paiements<br>`@JsonBackReference("contrat-paiements")` sur Paiement.contrat | Relation de **composition** : les paiements n'existent que par leur contrat (cascade ALL, orphanRemoval). Le contrat inclut ses paiements dans le JSON, mais chaque paiement ne sérialise pas son contrat parent pour éviter la boucle infinie. |
| **Agence ↔ Vehicule** | `@JsonManagedReference` sur Agence.vehicules<br>`@JsonBackReference` sur Vehicule.agence | L'agence est le parent : elle affiche ses véhicules dans la réponse GET /api/agences/{id}. Le véhicule ne sérialise pas son agence pour éviter la récursion. |
| **Agence ↔ Employe** | `@JsonManagedReference` sur Agence.employes<br>`@JsonBackReference` sur Employe.agence | Même logique : l'agence montre ses employés, mais l'employé ne renvoie pas l'agence complète. |
| **Client ↔ Reservation** | `@JsonManagedReference` sur Client.reservations<br>`@JsonBackReference` sur Reservation.client | Le client affiche ses réservations. La réservation ne sérialise pas le client complet pour éviter la boucle. |

### Différence entre @JsonBackReference et @JsonIgnore

| Annotation | Comportement | Quand l'utiliser |
|------------|--------------|------------------|
| **@JsonBackReference** | La propriété est **ignorée à la sérialisation** (GET) mais **acceptée à la désérialisation** (POST/PUT). | Sur le côté **enfant** d'une composition (ex. Paiement.contrat) : on peut créer un paiement avec un contrat dans le JSON, mais le GET d'un paiement ne renvoie pas le contrat. |
| **@JsonIgnore** | La propriété est **totalement ignorée** en lecture ET en écriture. | Sur une collection inverse qu'on ne veut jamais manipuler via JSON (ex. si Agence.vehicules était en lecture seule et qu'on ne voulait jamais recevoir cette liste dans un POST). |

**Important :** Ne pas utiliser `@JsonBackReference` sur `Reservation.client` rendrait impossible la création d'une réservation avec un client (le champ serait ignoré à la lecture du JSON), d'où l'utilisation de `@JsonBackReference` qui permet la désérialisation.

---

## C. Postman et Swagger

### Ce que chacun apporte

| Outil | Rôle | Exemple concret dans l'atelier |
|-------|------|-------------------------------|
| **Postman** | **Tests et validation** : permet de construire des requêtes HTTP manuellement, de vérifier les codes de retour, de valider les corps JSON, et de rejouer les scénarios. | Créé la collection « AutoLoc » avec dossiers « Contrats », « Paiements », etc. Test du scénario : POST /api/contrats avec paiements → vérification 201 → GET /api/paiements pour confirmer la cascade PERSIST. Test des cas d'erreur : GET /api/contrats/9999 → 404, POST avec id → 400. |
| **Swagger UI** | **Documentation interactive** : génère automatiquement la documentation de l'API à partir des annotations, présente les schémas JSON, et permet d'essayer les endpoints directement depuis l'interface web. | Accessible à http://localhost:8081/api/swagger-ui.html. Affiche les 9 groupes d'endpoints (Contrats, Paiements, Agences, etc.) avec leurs résumés (@Operation), codes de retour (@ApiResponses), et schémas des entités. Permet de tester POST /api/contrats directement depuis le navigateur. |

### Complémentarité

- **Postman** sert à **tester** : on écrit des assertions (`pm.test("Statut 200", ...)`), on vérifie le comportement métier, on sauvegarde des requêtes pour les rejouer pendant le développement.
- **Swagger UI** sert à **documenter** : il montre le contrat de l'API (routes, paramètres, réponses possibles) et reste synchronisé avec le code grâce à SpringDoc.

Exemple rencontré : Lors du test de la cascade Contrat → Paiement, **Postman** a permis de valider que les deux paiements étaient bien créés sans appel séparé à POST /api/paiements. **Swagger UI** documente ensuite que PaiementController n'expose que GET (405 Method Not Allowed sur POST /api/paiements).

---

## D. Anomalies SonarQube

Analyse effectuée sur le package `web.controller` avec « Analyze with SonarQube for IDE ».

| Anomalie détectée | Règle | Explication | Correction apportée |
|-------------------|-------|-------------|---------------------|
| **Imports inutilisés** | java:S1128 | Après ajout des annotations Swagger, certains imports temporaires (ex. anciens imports de test) sont restés dans les contrôleurs. Les imports inutilisés encombrent le code et réduisent la lisibilité. | Supprimé tous les imports inutilisés avec l'optimisation IntelliJ (Ctrl+Alt+O) dans tous les contrôleurs. |
| **Entité exposée directement** | java:S6813 (avertissement personnalisé) | Les contrôleurs exposent directement les entités JPA (`@RequestBody Contrat`, retour `Contrat`). Cela couple l'API à la structure de la base de données : tout changement de schéma impacte les clients, et les champs internes (associations LAZY, version JPA) sont visibles. | **Non corrigé dans cet atelier** : l'exposition directe des entités est volontaire pour simplifier l'apprentissage. Le Chapitre 10 introduira les DTO pour découpler l'API du modèle de données. |

**Note :** SonarQube a également signalé l'absence de validation explicite (`@Valid`, `@NotNull`) sur les paramètres `@RequestBody`. Cette validation sera ajoutée avec les DTO et Bean Validation dans un atelier ultérieur.

---

## E. Questions de compréhension

### 1. Quel composant reçoit toutes les requêtes, et comment retrouve-t-il la méthode du contrôleur à appeler ?

**Réponse :** Le **DispatcherServlet** reçoit toutes les requêtes HTTP.

**Fonctionnement :**
1. Une requête arrive : `POST /api/contrats`
2. Le DispatcherServlet consulte le **HandlerMapping**, qui a scanné toutes les classes `@RestController` au démarrage
3. Le HandlerMapping trouve la méthode annotée `@PostMapping` dans le contrôleur dont `@RequestMapping("/contrats")` correspond
4. Le DispatcherServlet appelle cette méthode avec les paramètres extraits (corps JSON via `@RequestBody`, variables de chemin via `@PathVariable`)
5. La valeur retournée est convertie en JSON par **Jackson**
6. Le DispatcherServlet renvoie la réponse HTTP avec le code de statut et le corps JSON

---

### 2. Pourquoi ContratController dépend-il de IContratService et non de ContratRepository ni de ContratServiceImpl ?

**Réponse :** Pour respecter l'**architecture en couches** et le **principe de dépendance aux abstractions** (Dependency Inversion Principle).

**Pourquoi pas ContratRepository ?**
- Le contrôleur ne doit pas accéder directement à la base de données : cela mélange la logique web et la logique métier.
- Les règles de validation (montantTotal >= 0, gestion des paiements) doivent être centralisées dans le service, pas répétées dans chaque contrôleur.

**Pourquoi pas ContratServiceImpl ?**
- Couplage à l'implémentation : si on change l'implémentation (ex. ContratServiceV2Impl avec cache), il faut modifier le contrôleur.
- Testabilité : en dépendant de l'interface, on peut facilement fournir un mock pour les tests unitaires.

**Pourquoi IContratService ?**
- Découplage : le contrôleur ne connaît que le contrat (interface), pas les détails d'implémentation.
- Évolutivité : on peut changer de service (ex. basculer sur une implémentation qui appelle un autre microservice) sans toucher au contrôleur.

---

### 3. Pourquoi un POST renvoie-t-il 201 et un DELETE 204 ? Pourquoi une ressource absente donne-t-elle 404 et non 500 ?

**Réponse :**

**POST → 201 Created**
- Le code 201 signale qu'une **nouvelle ressource a été créée**.
- Sémantiquement plus précis que 200 OK (qui signifie « succès » mais ne dit pas qu'une ressource a été ajoutée).
- Permet au client de distinguer une création d'une simple lecture réussie.

**DELETE → 204 No Content**
- Le code 204 indique un **succès sans corps de réponse**.
- Après suppression, il n'y a rien à renvoyer (l'objet n'existe plus), donc le corps est vide.
- 200 OK serait acceptable mais 204 est plus précis : il confirme l'absence de contenu intentionnelle.

**Ressource absente → 404 et non 500**
- **404 Not Found** : la ressource demandée n'existe pas. C'est une **erreur client** (mauvais ID fourni).
- **500 Internal Server Error** : erreur serveur imprévue (ex. base de données inaccessible, exception non gérée).
- Si GET /api/contrats/9999 renvoyait 500, le client penserait que le serveur est cassé, alors qu'en réalité l'ID 9999 n'existe simplement pas. Le 404 est la bonne sémantique.

---

### 4. Pourquoi PaiementController n'expose-t-il que des GET ? Quelle réponse reçoit un POST /api/paiements ?

**Réponse :** **PaiementController est en lecture seule** car les paiements sont gérés par **cascade** via leur contrat parent.

**Règle métier :** Un paiement n'existe que dans le contexte d'un contrat (composition avec `cascade = ALL`, `orphanRemoval = true`). Créer, modifier ou supprimer un paiement directement violerait cette règle.

**Gestion correcte :**
```java
// ✅ Créer un contrat avec ses paiements
POST /api/contrats
{
  "dateSignature": "2026-11-02",
  "montantTotal": 480.00,
  "valide": true,
  "paiements": [
    { "montant": 150.00, "datePaiement": "2026-11-02", "modePaiement": "CARTE" },
    { "montant": 330.00, "datePaiement": "2026-11-06", "modePaiement": "VIREMENT" }
  ]
}
// Les deux paiements sont persistés automatiquement par cascade PERSIST
```

**Réponse à POST /api/paiements :**
```
405 Method Not Allowed
```
Aucune méthode `@PostMapping` n'existe dans PaiementController, donc Spring renvoie 405 pour indiquer que la méthode HTTP POST n'est pas supportée sur cette ressource.

---

### 5. Quels risques présente l'exposition directe des entités JPA ? Pourquoi Contrat.paiements utilise-t-il @JsonManagedReference alors que Agence.vehicules utilise @JsonIgnore ?

**Réponse :**

**Risques de l'exposition directe des entités JPA :**

1. **Couplage API/Base de données** : tout changement de schéma (ajout d'une colonne, renommage) impacte directement le JSON de l'API et casse les clients.
2. **Champs internes visibles** : les associations LAZY, les proxies Hibernate, les champs techniques (version, dateCreation) apparaissent dans le JSON.
3. **Sécurité** : impossible de masquer des champs sensibles (ex. mot de passe) sans annotations sur l'entité, ce qui mélange les préoccupations.
4. **Validation difficile** : les règles de validation JSON (ex. email obligatoire en création mais optionnel en mise à jour) ne peuvent pas différer selon l'opération.

**Solution (non implémentée ici) :** Utiliser des DTO (Data Transfer Objects) qui représentent uniquement ce que l'API expose, avec des mappers pour convertir entre entités et DTO.

---

**Pourquoi Contrat.paiements utilise @JsonManagedReference ?**

- **Composition forte** : les paiements sont créés **dans le JSON du contrat** et n'existent pas indépendamment.
- `@JsonManagedReference` permet d'inclure la liste des paiements dans la réponse GET /api/contrats/{id} et dans le corps POST /api/contrats.
- Le cycle est évité par `@JsonBackReference` côté Paiement : le paiement ne renvoie pas son contrat.

**Pourquoi Agence.vehicules n'utilise pas @JsonManagedReference (hypothèse si @JsonIgnore était utilisé) ?**

Dans notre implémentation actuelle, **Agence.vehicules utilise `@JsonManagedReference`**, donc l'agence affiche ses véhicules.

Si on avait utilisé `@JsonIgnore` à la place :
- **Création séparée** : les véhicules sont créés via POST /api/vehicules avec `{ "agence": {"idAgence": 1} }` (référence par ID).
- Pas besoin d'inclure la liste complète des véhicules dans GET /api/agences/{id} (trop volumineux).
- Le client fait un appel séparé GET /api/vehicules?agenceId=1 pour lister les véhicules d'une agence.

**Choix fait dans notre projet :** `@JsonManagedReference` sur Agence.vehicules et Agence.employes pour que GET /api/agences/{id} renvoie la liste complète (pratique pour la démonstration, mais peut poser des problèmes de performance en production avec beaucoup de véhicules).
