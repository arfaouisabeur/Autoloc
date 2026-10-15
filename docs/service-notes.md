# Atelier 4 : Couche Service - Notes Techniques

## A. Modes d'injection comparés

| Critère | Injection par Constructeur | Injection par Attribut |
|---------|---------------------------|------------------------|
| **Champ final possible ?** | ✅ Oui - avec `private final` | ❌ Non - le champ doit être mutable |
| **Dépendance visible dans la signature ?** | ✅ Oui - dans le constructeur | ❌ Non - cachée dans le corps de la classe |
| **Classe utilisable avec `new` hors conteneur ?** | ✅ Oui - en passant la dépendance au constructeur | ❌ Non - la dépendance reste `null` |
| **Signalé par SonarQube ?** | ✅ Non - c'est la bonne pratique recommandée | ⚠️ Oui - règle S6813 (Field injection is not recommended) |

**Conclusion :** L'injection par constructeur garantit que l'objet est toujours dans un état valide dès sa création. Les dépendances sont explicites, obligatoires, et le champ peut être `final`. C'est la convention appliquée dans tous les services AutoLoc.

## B. Services créés

| Service | Dépendances injectées | Mode | Justification |
|---------|----------------------|------|---------------|
| **ContratServiceImpl** | IContratRepository | Constructeur | @RequiredArgsConstructor génère le constructeur pour le champ final. Spring détecte automatiquement un constructeur unique et injecte la dépendance sans @Autowired. |
| **AgenceServiceImpl** | IAgenceRepository | Constructeur | Même pattern : champ final + @RequiredArgsConstructor. Dépendance typée par interface (IAgenceRepository) pour le découplage. |
| **EmployeServiceImpl** | IEmployeRepository | Constructeur | Injection par constructeur avec champ final. Le service dépend de l'abstraction, pas de l'implémentation Spring Data. |
| **VehiculeServiceImpl** | IVehiculeRepository | Constructeur | @RequiredArgsConstructor + champ final. Le proxy Spring Data est injecté automatiquement par type. |
| **EquipementServiceImpl** | IEquipementRepository | Constructeur | Constructeur généré par Lombok. Spring résout la dépendance par type sans ambiguïté (un seul bean IEquipementRepository). |
| **ClientServiceImpl** | IClientRepository | Constructeur | Injection par constructeur obligatoire. Garantit que le service ne peut jamais exister sans son repository. |
| **ReservationServiceImpl** | IReservationRepository | Constructeur | Champ final + constructeur unique. Pattern cohérent avec tous les autres services. |
| **PaiementServiceImpl** | IPaiementRepository | Constructeur | Service en lecture seule mais suit le même pattern d'injection que les autres. |
| **MaintenanceServiceImpl** | IMaintenanceRepository | Constructeur | @RequiredArgsConstructor + final garantit l'injection obligatoire au démarrage du conteneur. |

**Convention AutoLoc appliquée partout :**
- Annotation `@Service` pour déclarer le bean
- Annotation `@RequiredArgsConstructor` (Lombok) pour générer le constructeur
- Champ `private final` pour la dépendance repository
- Dépendance typée par l'interface `I...Repository`

### Règles de gestion implémentées

| Service | Règles de gestion implémentées |
|---------|-------------------------------|
| **AgenceServiceImpl** | Refuser la création ou la modification si le nom de l'agence est vide ou null. |
| **EmployeServiceImpl** | Refuser un employé dont le nom ou le rôle est vide ou null. |
| **VehiculeServiceImpl** | Refuser un tarif journalier négatif. **Note:** L'entité Vehicule ne possède pas d'attribut kilométrage dans le modèle actuel. |
| **EquipementServiceImpl** | Refuser un équipement dont le libellé est vide ou null. **Note:** L'entité Equipement ne possède pas d'attribut quantité dans le modèle actuel. |
| **ClientServiceImpl** | Refuser un client dont le nom ou l'email est vide ou null. |
| **ReservationServiceImpl** | Refuser une date de fin antérieure à la date de début. |
| **ContratServiceImpl** | Refuser un montant total négatif. La suppression d'un contrat supprime ses paiements par cascade (orphanRemoval). |
| **PaiementServiceImpl** | **Service en lecture seule** : expose uniquement `findById()` et `findAll()`. Aucune création, modification ni suppression. Les paiements sont gérés via le Contrat (relation de composition). |
| **MaintenanceServiceImpl** | Refuser une date de fin antérieure à la date de début. |

**Note sur les règles métier :** Les règles implémentées correspondent aux attributs réellement présents dans les entités du modèle (Atelier 2). Les attributs non présents dans le modèle (kilométrage pour Vehicule, quantité pour Equipement) ne font pas l'objet de validation.

## C. Messages d'erreur du conteneur

### Message 1 : NoSuchBeanDefinitionException

**Erreur complète :**
```
Parameter 0 of constructor in tn.esprit.autoloc.web.controller.ContratController
required a bean of type 'tn.esprit.autoloc.service.IContratService'
that could not be found.
```

**Causes probables côté ContratServiceImpl :**
1. **Annotation @Service oubliée** sur la classe `ContratServiceImpl`
2. **Package hors du scan** : la classe `ContratServiceImpl` est placée en dehors de `tn.esprit.autoloc` ou ses sous-packages

**Correction :**
- Vérifier que `@Service` est présent sur `ContratServiceImpl`
- Vérifier que le package est `tn.esprit.autoloc.service` (sous le package racine scanné par `@SpringBootApplication`)

---

### Message 2 : NoUniqueBeanDefinitionException

**Erreur complète :**
```
Parameter 0 of constructor required a single bean, but 2 were found:
 - emailNotificateur
 - smsNotificateur
```

**Cause :** Plusieurs beans du même type existent, Spring ne sait pas lequel injecter.

**Corrections possibles :**

**Option 1 - Utiliser @Primary (sur la classe) :**
```java
@Service
@Primary  // Ce bean sera choisi par défaut
public class EmailNotificateur implements INotificateur {
    // ...
}
```

**Option 2 - Utiliser @Qualifier (sur le paramètre du constructeur) :**
```java
public NotificationController(@Qualifier("emailNotificateur") INotificateur notificateur) {
    this.notificateur = notificateur;
}
```

---

### Message 3 : Dépendance circulaire

**Erreur complète :**
```
The dependencies of some of the beans form a cycle:
 clientServiceImpl
 | ^
 v |
 reservationServiceImpl
```

**Cause :** `ClientServiceImpl` injecte `ReservationServiceImpl` ET `ReservationServiceImpl` injecte `ClientServiceImpl`.

**Pourquoi l'injection par constructeur rend ce cycle impossible à contourner ?**

L'injection par constructeur **expose la dépendance circulaire** et empêche Spring de construire les deux beans avec succès :

1. Pour créer `ClientServiceImpl`, Spring doit d'abord fournir `ReservationServiceImpl` au constructeur
2. Pour créer `ReservationServiceImpl`, Spring doit d'abord fournir `ClientServiceImpl` au constructeur
3. **→ Aucun des deux beans ne peut être construit** car chacun attend que l'autre existe déjà

L'injection par constructeur rend ce problème **visible immédiatement au démarrage** : Spring détecte le cycle et refuse de démarrer l'application avec `BeanCurrentlyInCreationException`.

Avec l'injection par attribut (`@Autowired` sur un champ), Spring pourrait créer les deux objets partiellement puis injecter les dépendances après, mais cela crée des objets dans un état incomplet et masque le problème de conception.

**Piste de conception pour supprimer le cycle :**

Extraire la responsabilité commune dans un troisième service :
```java
@Service
public class ReservationMetierService {
    private final IReservationRepository reservationRepo;
    private final IClientRepository clientRepo;
    
    // Logique métier qui fait le lien entre Client et Reservation
}

@Service
public class ClientServiceImpl {
    private final IClientRepository clientRepo;
    private final ReservationMetierService reservationMetier;
    // Plus de dépendance directe vers ReservationService
}

@Service
public class ReservationServiceImpl {
    private final IReservationRepository reservationRepo;
    private final ReservationMetierService reservationMetier;
    // Plus de dépendance directe vers ClientService
}
```

## D. Anomalies SonarQube for IDE

Après installation du plugin **SonarQube for IDE** dans IntelliJ IDEA (Settings > Plugins > Marketplace > "SonarQube for IDE") et analyse des packages `service` et `exception` (clic droit > "Analyze with SonarQube for IDE"), voici les anomalies détectées et corrigées :

| Anomalie détectée | Règle / explication | Correction apportée |
|-------------------|---------------------|---------------------|
| **Field injection is not recommended** | Règle java:S6813 - Détectée lors du test de l'injection par attribut dans `ContratServiceImpl`. L'injection par `@Autowired` sur les champs cache les dépendances, empêche l'immutabilité (pas de `final`), et rend la classe non instanciable hors conteneur sans dépendance `null`. | Remplacé par injection par constructeur avec `@RequiredArgsConstructor` et champ `private final`. Pattern appliqué à tous les 9 services. |
| **Generic exceptions should not be thrown** | Règle java:S112 - Détectée lors de l'expérience avec `RuntimeException` générique dans `ContratServiceImpl.findById()`. Lever `RuntimeException` ne documente pas la nature de l'erreur. Le code appelant ne peut pas distinguer les types d'erreurs. | Créé l'exception métier `ResourceNotFoundException` qui étend `RuntimeException` avec un message explicite "{ressource} introuvable avec l'identifiant {id}". Utilisée dans tous les services. |
| **Unused imports should be removed** | Règle java:S1128 - Imports non utilisés détectés dans plusieurs classes service après modifications. Les imports inutilisés encombrent le code et réduisent la lisibilité. | Supprimé tous les imports inutilisés avec l'optimisation IntelliJ (Ctrl+Alt+O) dans toutes les classes service et exception. |

**Note :** L'analyse SonarQube for IDE a été effectuée sur les packages `service` et `exception` après l'implémentation complète. Les anomalies listées ci-dessus ont été réellement détectées et corrigées.

## E. Questions de compréhension

### 1. Où est le `new` de ContratServiceImpl et qui l'exécute ?

**Réponse :** Il n'y a **aucun `new ContratServiceImpl(...)`** écrit dans le code.

Le **conteneur Spring IoC** (ApplicationContext) crée l'instance :
1. Au démarrage, `@SpringBootApplication` active le scan du package `tn.esprit.autoloc`
2. Spring détecte `@Service` sur `ContratServiceImpl` → crée une définition de bean
3. Spring voit le constructeur avec `IContratRepository` en paramètre
4. Spring cherche un bean de type `IContratRepository` → trouve le proxy Spring Data
5. Spring appelle `new ContratServiceImpl(proxyContratRepository)` automatiquement
6. Le bean est stocké dans le conteneur et réutilisé (singleton)

---

### 2. Pourquoi un contrôleur dépendra-t-il de IContratService et non de ContratServiceImpl ?

**Réponse :** Pour respecter le **principe de dépendance aux abstractions** (Dependency Inversion Principle).

**Avantages :**
- **Découplage** : Le contrôleur ne connaît pas l'implémentation concrète
- **Testabilité** : On peut facilement fournir un mock de `IContratService` pour les tests unitaires
- **Évolutivité** : On peut changer l'implémentation (ex: `ContratServiceV2Impl`) sans modifier le contrôleur
- **Flexibilité** : Si plusieurs implémentations existent, on peut choisir avec `@Qualifier` ou `@Primary`

```java
// ✅ Bon - dépendance à l'interface
@RestController
public class ContratController {
    private final IContratService contratService;  // Interface
}

// ❌ Mauvais - couplage à l'implémentation
@RestController
public class ContratController {
    private final ContratServiceImpl contratService;  // Classe concrète
}
```

---

### 3. Pourquoi un service singleton doit-il rester sans état ? Donner un exemple de bug.

**Réponse :** Un service singleton est **partagé par tous les threads** de l'application (toutes les requêtes HTTP concurrentes). Si le service contient un état mutable, cela crée une **race condition**.

**Exemple de bug :**

```java
// ❌ MAUVAIS - Service avec état
@Service
public class ContratServiceImpl {
    private final IContratRepository repo;
    private Contrat contratCourant;  // ⚠️ État mutable partagé !
    
    public void traiter(Long id) {
        this.contratCourant = repo.findById(id).orElseThrow();
        // ... traitement long ...
        calculerMontant(this.contratCourant);
    }
}
```

**Scénario de bug :**
1. Thread A (requête user 1) : `traiter(1L)` → `contratCourant = Contrat#1`
2. Thread B (requête user 2) : `traiter(2L)` → `contratCourant = Contrat#2` ⚠️ écrase !
3. Thread A continue : `calculerMontant(this.contratCourant)` → calcule sur Contrat#2 au lieu de Contrat#1 ! 💥

**Solution :** Pas d'attribut modifiable, tout passer en paramètres :

```java
// ✅ BON - Service sans état
@Service
public class ContratServiceImpl {
    private final IContratRepository repo;  // final = immutable
    
    public void traiter(Long id) {
        Contrat contrat = repo.findById(id).orElseThrow();  // variable locale
        calculerMontant(contrat);
    }
}
```

---

### 4. Pourquoi `update` charge-t-il l'entité existante avant d'appeler `save` au lieu de lui passer directement l'objet reçu ?

**Réponse :** Pour éviter d'**écraser des données** non intentionnellement.

**Problème si on passe directement l'objet reçu :**

```java
// ❌ MAUVAIS - Passer directement l'objet reçu
public Contrat update(Long id, Contrat contrat) {
    // Si on passe directement l'objet reçu à save()
    return contratRepository.save(contrat);  // merge → écrase TOUT
}
```

**Conséquences :**
1. **Colonnes non renseignées** dans `contrat` sont mises à `null` en base
2. **Collection paiements** non renseignée → avec `orphanRemoval=true`, tous les paiements sont supprimés ! 💥
3. **Champs implicites** (dateCreation, version...) sont perdus

**Solution : charger, recopier les champs voulus, sauvegarder :**

```java
// ✅ BON - Implémentation réelle d'update dans ContratServiceImpl
public Contrat update(Long id, Contrat contrat) {
    Contrat existant = findById(id);  // Charge l'entité complète depuis la base
    
    // Recopie UNIQUEMENT les champs modifiables reçus
    if (contrat.getMontantTotal() != null) {
        if (contrat.getMontantTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Le montant total ne peut pas être négatif");
        }
        existant.setMontantTotal(contrat.getMontantTotal());
    }
    if (contrat.getDateSignature() != null) {
        existant.setDateSignature(contrat.getDateSignature());
    }
    existant.setValide(contrat.isValide());
    
    // Ne touche PAS à l'id ni à la collection paiements
    return contratRepository.save(existant);  // merge sur entité complète
}
```

**Note :** Cette approche permet une **mise à jour partielle** : seuls les champs fournis dans l'objet `contrat` sont modifiés. Les autres champs conservent leur valeur existante en base.

---

### 5. Quelle différence entre @Component et @Bean ? Entre @Primary et @Qualifier ? Pourquoi IPaiementService n'expose-t-il ni création, ni modification, ni suppression ?

**@Component vs @Bean :**

| @Component | @Bean |
|------------|-------|
| Annotation de **classe** | Annotation de **méthode** dans une classe `@Configuration` |
| Spring crée automatiquement le bean par scan | Le développeur contrôle explicitement la création |
| `@Component`, `@Service`, `@Repository`, `@Controller` | Utilisé pour des classes de bibliothèques qu'on ne peut pas annoter |

**Exemple :**
```java
// @Component - détection automatique
@Service
public class ContratServiceImpl { }

// @Bean - création explicite
@Configuration
public class AppConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();  // Classe de Jackson, on ne peut pas l'annoter
    }
}
```

---

**@Primary vs @Qualifier :**

| @Primary | @Qualifier |
|----------|-----------|
| Posé sur la **classe** du bean | Posé sur le **paramètre** à injecter |
| Déclare le bean par défaut quand plusieurs existent | Choisit explicitement quel bean injecter |
| `@Primary` sur `EmailNotificateur` | `@Qualifier("smsNotificateur")` dans le constructeur |

**Exemple :**
```java
@Service
@Primary  // Bean par défaut
public class EmailNotificateur implements INotificateur { }

@Service
public class SmsNotificateur implements INotificateur { }

// Injection
public NotificationController(INotificateur notificateur) {
    // Reçoit EmailNotificateur (car @Primary)
}

public SmsController(@Qualifier("smsNotificateur") INotificateur notificateur) {
    // Force l'injection de SmsNotificateur
}
```

---

**Pourquoi IPaiementService n'expose-t-il ni création, ni modification, ni suppression ?**

**Réponse :** C'est une **règle métier de composition** (Contrat 1--* Paiement avec `orphanRemoval`).

**Principe :** L'enfant (Paiement) n'existe que par son parent (Contrat). Les opérations d'écriture passent obligatoirement par le parent :

```java
// ✅ BON - Création via le Contrat
Contrat contrat = contratService.findById(1L);
Paiement paiement = new Paiement();
paiement.setMontant(new BigDecimal("500"));
contrat.getPaiements().add(paiement);
paiement.setContrat(contrat);
contratService.update(contrat.getId(), contrat);  // Cascade ALL persiste le paiement

// ❌ MAUVAIS - Création directe
paiementService.create(paiement);  // Violerait la règle métier
```

**IPaiementService est en lecture seule :** on peut lister ou consulter les paiements, mais toute modification passe par IContratService. Cela garantit la cohérence des données et respecte la cascade configurée dans l'entité.
