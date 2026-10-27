package tn.esprit.sabeurarfaoui4cce11.service.paiement;

import tn.esprit.sabeurarfaoui4cce11.domain.Paiement;

/**
 * Interface pour les modes de paiement remboursables.
 * 
 * PRINCIPE LSP : Seuls les modes de paiement qui PEUVENT RÉELLEMENT
 * être remboursés implémentent cette interface.
 * 
 * Implémentée par :
 * - PaiementCarte ✅
 * - PaiementVirement ✅
 * 
 * NON implémentée par :
 * - PaiementEspeces ❌ (impossible de rembourser du cash)
 * 
 * Avantages :
 * - ✅ Sécurité à la compilation : impossible d'appeler rembourser() sur un paiement en espèces
 * - ✅ Respect de LSP : pas d'exception UnsupportedOperationException à l'exécution
 * - ✅ Clarté du code : l'interface exprime explicitement la capacité de remboursement
 * 
 * Mission 3 - Atelier 6 : Principe LSP appliqué
 */
public interface IRemboursable {

    /**
     * Rembourse un paiement
     * 
     * Cette méthode ne sera JAMAIS appelée sur un mode de paiement
     * qui ne l'implémente pas (sécurité à la compilation).
     * 
     * @param paiement Le paiement à rembourser
     * @throws IllegalStateException Si le paiement a déjà été remboursé
     * @throws IllegalArgumentException Si le paiement est invalide
     */
    void rembourser(Paiement paiement);
}
