# Stratégies de chargement et de cascade --- AutoLoc API

**Atelier 2 --- Associations JPA**

## 1. Objectif

Ce document présente les stratégies de chargement et de propagation du
cycle de vie retenues pour les associations JPA du modèle AutoLoc. Les
choix sont fondés sur les associations définies dans le tableau de
l'Atelier 2 et sur les règles de gestion décrites dans le fascicule.

## 2. Synthèse des stratégies retenues

  -------------------------------------------------------------------------------
  Association       Stratégie de      Cascade                  Justification
                    chargement                                 
  ----------------- ----------------- ------------------------ ------------------
  **Contrat →       `LAZY`            `ALL` et                 Un paiement dépend
  Paiement**                          `orphanRemoval = true`   de son contrat. La
                                                               cascade propage
                                                               les opérations de
                                                               persistance et de
                                                               suppression aux
                                                               paiements
                                                               associés.
                                                               `orphanRemoval`
                                                               supprime un
                                                               paiement retiré de
                                                               la collection du
                                                               contrat.

  **Agence →        `LAZY`            Aucune                   Le véhicule
  Véhicule**                                                   possède son propre
                                                               cycle de vie et
                                                               peut être
                                                               réaffecté à une
                                                               autre agence. La
                                                               suppression d'une
                                                               agence ne doit
                                                               donc pas entraîner
                                                               automatiquement
                                                               celle de ses
                                                               véhicules.

  **Agence →        `LAZY`            Aucune                   L'employé est géré
  Employé**                                                    indépendamment de
                                                               l'agence. La
                                                               suppression d'une
                                                               agence ne doit pas
                                                               supprimer
                                                               automatiquement le
                                                               dossier de
                                                               l'employé.

  **Véhicule ↔      `LAZY`            Aucune                   Un équipement peut
  Équipement**                                                 être associé à
                                                               plusieurs
                                                               véhicules. La
                                                               suppression d'un
                                                               véhicule ne doit
                                                               pas supprimer un
                                                               équipement
                                                               réutilisable. Un
                                                               `Set` est retenu
                                                               pour représenter
                                                               une collection
                                                               sans doublons.

  **Client →        `LAZY`            `PERSIST`                La persistance
  Réservation**                                                d'un client peut
                                                               propager
                                                               l'enregistrement
                                                               des nouvelles
                                                               réservations
                                                               associées. La
                                                               cascade de
                                                               suppression n'est
                                                               pas activée, afin
                                                               de ne pas
                                                               supprimer
                                                               automatiquement
                                                               l'historique des
                                                               réservations.

  **Réservation →   `LAZY`            Aucune                   Le véhicule
  Véhicule**                                                   possède un cycle
                                                               de vie indépendant
                                                               de celui de la
                                                               réservation. La
                                                               suppression d'une
                                                               réservation ne
                                                               doit pas supprimer
                                                               le véhicule
                                                               associé.

  **Réservation ↔   `LAZY`            `ALL` côté `Reservation` Le contrat est
  Contrat**                                                    associé à une
                                                               réservation unique
                                                               dans le modèle. Le
                                                               choix de cascade
                                                               suit le tableau de
                                                               l'atelier : les
                                                               opérations sont
                                                               propagées depuis
                                                               `Reservation` vers
                                                               `Contrat`.

  **Véhicule →      `LAZY`            `PERSIST`                La persistance
  Maintenance**                                                d'un véhicule peut
                                                               propager
                                                               l'enregistrement
                                                               des maintenances
                                                               associées. La
                                                               suppression n'est
                                                               pas propagée afin
                                                               de préserver
                                                               l'historique de
                                                               maintenance.
  -------------------------------------------------------------------------------

## 3. Justification des choix

### 3.1 Stratégie de chargement : `LAZY`

La stratégie `FetchType.LAZY` est retenue pour toutes les associations
du modèle conformément au tableau de l'atelier. Les données associées
sont chargées à la demande, lorsque l'application accède à
l'association, plutôt que systématiquement lors du chargement de
l'entité principale.

Cette stratégie limite le chargement de données qui ne sont pas
nécessaires à une opération donnée. L'accès à une association `LAZY`
doit toutefois être effectué dans un contexte de persistance approprié ;
sinon, une `LazyInitializationException` peut se produire.

### 3.2 Propagation des opérations : `CascadeType`

Les options de cascade sont choisies en fonction du cycle de vie métier
des entités :

-   **`CascadeType.ALL` avec `orphanRemoval = true`** pour
    `Contrat → Paiement`, car le paiement dépend du contrat auquel il
    est rattaché.
-   **`CascadeType.PERSIST`** pour `Client → Reservation` et
    `Vehicule → Maintenance`, conformément au tableau de l'atelier.
-   **Aucune cascade** pour les autres associations listées, afin de
    préserver l'indépendance du cycle de vie des entités associées.

### 3.3 Côté propriétaire et côté inverse

Dans une association `@ManyToOne` / `@OneToMany`, le côté `@ManyToOne`
porte la clé étrangère et constitue le côté propriétaire. Le côté
`@OneToMany` utilise `mappedBy` pour désigner l'attribut correspondant
dans l'entité propriétaire. Cette configuration évite de mapper les deux
côtés comme des associations indépendantes.

### 3.4 Suppression et `orphanRemoval`

`CascadeType.REMOVE` propage la suppression du parent vers les entités
associées. `orphanRemoval = true` concerne le cas où une entité enfant
est retirée de la collection du parent : l'enfant est alors supprimé de
la base de données. Dans l'association `Contrat → Paiement`, ces
mécanismes répondent au cycle de vie attendu des paiements.

## 4. Conventions de codage

-   Utiliser `@Getter` et `@Setter` de manière ciblée plutôt que `@Data`
    sur les entités ayant des associations bidirectionnelles, afin
    d'éviter les appels récursifs générés par `toString()` ou
    `hashCode()`.
-   Utiliser un `Set` pour l'association `Vehicule ↔ Equipement`,
    conformément à la recommandation de l'atelier.
-   Conserver `FetchType.LAZY` pour toutes les associations conformément
    au tableau du TP.

## 5. Conclusion

Les associations AutoLoc sont configurées pour maîtriser le chargement
des données et préserver le cycle de vie propre à chaque entité. Les
stratégies `LAZY`, les options de cascade et `orphanRemoval` sont
appliqués selon les choix indiqués dans l'Atelier 2. La configuration
finale doit être vérifiée avec le schéma généré par Hibernate et le
comportement de l'application.
