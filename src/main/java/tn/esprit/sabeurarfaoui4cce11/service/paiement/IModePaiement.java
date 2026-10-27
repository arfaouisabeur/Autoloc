package tn.esprit.sabeurarfaoui4cce11.service.paiement;

import tn.esprit.sabeurarfaoui4cce11.domain.Paiement;

import java.math.BigDecimal;

/**
 * Interface de base pour tous les modes de paiement.
 * 
 * PRINCIPE LSP : Cette interface ne contient QUE les opérations communes
 * à TOUS les modes de paiement, sans exception.
 * 
 * Tous les modes de paiement (CARTE, VIREMENT, ESPECES) peuvent effectuer
 * un paiement, donc cette méthode est présente dans l'interface de base.
 * 
 * Mission 3 - Atelier 6 : Respect du principe LSP (Liskov Substitution Principle)
 */
public interface IModePaiement {

    /**
     * Effectue un paiement pour un montant donné
     * 
     * @param montant Le montant à payer
     * @return L'objet Paiement créé
     */
    Paiement payer(BigDecimal montant);
    
    /**
     * Retourne le type de mode de paiement
     * 
     * @return Le type de paiement (CARTE, VIREMENT, ESPECES)
     */
    tn.esprit.sabeurarfaoui4cce11.domain.ModePaiement getType();
}
