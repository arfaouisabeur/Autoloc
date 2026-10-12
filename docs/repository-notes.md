# Atelier 3 : Couche Repository - Notes Techniques

## 1. Choix de l'interface JpaRepository

Toutes les interfaces repository du projet AutoLoc étendent **JpaRepository<Entity, Long>**.

| Interface Repository | Étend | Justification |
|---------------------|-------|---------------|
| IAgenceRepository | JpaRepository<Agence, Long> | CRUD complet + pagination/tri + méthodes JPA (flush, saveAndFlush) |
| IEmployeRepository | JpaRepository<Employe, Long> | CRUD complet + pagination/tri + méthodes JPA (flush, saveAndFlush) |
| IVehiculeRepository | JpaRepository<Vehicule, Long> | CRUD complet + pagination/tri + méthodes JPA (flush, saveAndFlush) |
| IEquipementRepository | JpaRepository<Equipement, Long> | CRUD complet + pagination/tri + méthodes JPA (flush, saveAndFlush) |
| IClientRepository | JpaRepository<Client, Long> | CRUD complet + pagination/tri + méthodes JPA (flush, saveAndFlush) |
| IReservationRepository | JpaRepository<Reservation, Long> | CRUD complet + pagination/tri + méthodes JPA (flush, saveAndFlush) |
| IContratRepository | JpaRepository<Contrat, Long> | CRUD complet + pagination/tri + méthodes JPA (flush, saveAndFlush) |
| IPaiementRepository | JpaRepository<Paiement, Long> | CRUD complet + pagination/tri + méthodes JPA (flush, saveAndFlush) |
| IMaintenanceRepository | JpaRepository<Maintenance, Long> | CRUD complet + pagination/tri + méthodes JPA (flush, saveAndFlush) |

**Pourquoi JpaRepository ?**

JpaRepository est l'interface la plus complète de Spring Data JPA. Elle hérite de :
- **ListCrudRepository** : opérations CRUD avec retour en List<T> au lieu de Iterable<T>
- **ListPagingAndSortingRepository** : tri avec Sort et pagination avec Pageable
- Ajoute des méthodes JPA spécifiques : flush(), saveAndFlush(), getReferenceById(), suppressions en lot

Pour AutoLoc, ce choix offre :
- Toutes les méthodes CRUD héritées : save(), findById(), findAll(), deleteById(), count()
- Collections retournées en List (plus pratique que Iterable)
- Support de la pagination et du tri pour lister véhicules, clients, réservations
- Méthodes JPA comme flush() pour forcer l'écriture immédiate en base

**Alternative non retenue :**
- CrudRepository aurait suffi pour le CRUD simple, mais renvoie des Iterable et n'offre pas pagination/tri
- Repository aurait nécessité de recopier manuellement chaque signature de méthode

## 2. Analyse SonarQube for IDE

### Installation et configuration
Plugin **SonarQube for IDE** installé dans IntelliJ IDEA (Settings > Plugins > Marketplace > rechercher "SonarQube for IDE").

### Anomalies détectées et corrections apportées

| Anomalie SonarQube détectée | Règle / explication | Correction apportée |
|----------------------------|---------------------|---------------------|
| **Imports non utilisés dans DataInitializer** | Rule java:S1128 - Les imports inutilisés encombrent le code et nuisent à la lisibilité | Suppression des imports non utilisés détectés par SonarQube dans le fichier DataInitializer.java |
| **Repository interfaces sont vides** | Info SonarQube - Les interfaces vides peuvent sembler inutiles | Aucune correction nécessaire : comportement normal de Spring Data JPA qui génère automatiquement l'implémentation via proxy au démarrage |
| **Utilisation de types primitifs vs wrappers** | Best practice java:S1610 - Préférer les wrappers (Boolean) aux primitifs (boolean) dans les entités JPA | Le champ `valide` dans Contrat utilise `boolean` primitif. Acceptable pour JPA mais `Boolean` permettrait de distinguer null/true/false |

**Note importante :**
- Les interfaces repository Spring Data JPA sont intentionnellement vides
- Spring Data génère automatiquement l'implémentation au démarrage de l'application
- Les méthodes CRUD (save, findById, deleteById, findAll, etc.) sont héritées de JpaRepository
- Les query methods personnalisées seront ajoutées dans les prochains ateliers

## 3. Bonnes pratiques appliquées

✅ **Conventions de nommage**
- Préfixe I pour toutes les interfaces repository (IVehiculeRepository, IContratRepository...)
- Convention claire qui distingue interfaces et implémentations

✅ **Annotations**
- Pas d'annotation @Repository : Spring Data JPA détecte automatiquement les interfaces qui étendent Repository
- Les entités utilisent @Entity, @Id, @GeneratedValue, @ManyToOne, @OneToMany, etc.

✅ **Lombok**
- @Getter @Setter @NoArgsConstructor @AllArgsConstructor sur toutes les entités
- Pas d'utilisation de @Data (évite les problèmes de StackOverflowError avec les relations bidirectionnelles)

✅ **Organisation en couches**
- Package domain : entités JPA et énumérations
- Package repository : interfaces Spring Data JPA
- Séparation claire des responsabilités

✅ **Gestion des relations**
- FetchType.LAZY sur toutes les associations (performances)
- CascadeType et orphanRemoval configurés selon les règles métier
- Collections initialisées avec new ArrayList<>() ou new HashSet<>()

## 4. Comportement des méthodes CRUD

### save(Entity entity)
Spring Data JPA détermine s'il faut utiliser persist() ou merge() selon sa stratégie de détection des nouvelles entités. L'entité retournée doit être utilisée car elle peut différer de l'objet passé à save().

**Comportement typique :**
- Si l'id est null : persist → INSERT en base, l'id généré est renseigné dans l'objet retourné
- Si l'id est renseigné : merge → SELECT puis UPDATE si des changements sont détectés

### findById(Long id)
- **Retour** : Optional<Entity> (jamais null)
- **Usage correct** :

```java
Optional<Vehicule> optVehicule = vehiculeRepository.findById(1L);
if (optVehicule.isPresent()) {
    Vehicule v = optVehicule.get();
    // traiter le véhicule
}
// ou
Vehicule v = vehiculeRepository.findById(1L)
    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
```

### deleteById(Long id)
- Supprime l'entité par son id
- Depuis Spring Data 3 : aucune exception si l'id n'existe pas
- La suppression passe par le contexte de persistance : **cascade REMOVE et orphanRemoval s'appliquent**

### deleteAllInBatch() - ⚠️ Attention
- Suppression en une seule requête SQL DELETE
- **Contourne le contexte de persistance** : cascade et orphanRemoval ne s'appliquent pas
- À éviter pour les entités avec relations en cascade (exemple : supprimer des Contrats laisserait des Paiements orphelins)

## 5. Prochaines étapes (Ateliers 4 et 5)

Dans les prochains ateliers, nous ajouterons des query methods dérivées et des requêtes personnalisées basées sur les propriétés réelles des entités AutoLoc :

**Exemples de query methods dérivées possibles :**

```java
// IVehiculeRepository
List<Vehicule> findByStatut(StatutVehicule statut);
List<Vehicule> findByCategorie(CategorieVehicule categorie);
List<Vehicule> findByMarque(String marque);

// IClientRepository
Optional<Client> findByEmail(String email);
List<Client> findByNomContaining(String nom);

// IReservationRepository
List<Reservation> findByDateDebutBetween(LocalDate debut, LocalDate fin);
List<Reservation> findByStatut(StatutReservation statut);
```

**Requêtes personnalisées avec @Query :**

```java
@Query("SELECT v FROM Vehicule v WHERE v.agence.nom = :nomAgence")
List<Vehicule> rechercherVehiculesParAgence(@Param("nomAgence") String nomAgence);
```

**Pagination et tri :**

```java
Page<Vehicule> page = vehiculeRepository.findAll(
    PageRequest.of(0, 10, Sort.by("modele").ascending())
);
```

Ces fonctionnalités seront implémentées selon les besoins métier dans les ateliers suivants.
