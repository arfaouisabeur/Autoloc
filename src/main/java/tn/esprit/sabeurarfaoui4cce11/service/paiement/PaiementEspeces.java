package tn.esprit.sabeurarfaoui4cce11.service.paiement;

import org.springframework.stereotype.Component;
import tn.esprit.sabeurarfaoui4cce11.domain.ModePaiement;
import tn.esprit.sabeurarfaoui4cce11.domain.Paiement;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Implémentation du mode de paiement en ESPÈCES.
 * 
 * ⚠️ IMPORTANT : Cette classe implémente UNIQUEMENT IModePaiement.
 * Elle N'implémente PAS IRemboursable car il est impossible de rembourser du cash.
 * 
 * PRINCIPE LSP RESPECTÉ :
 * - ✅ PAS d'exception UnsupportedOperationException
 * - ✅ Le compilateur EMPÊCHE d'appeler rembourser() sur cette classe
 * - ✅ Le code client ne peut pas violer le contrat (sécurité à la compilation)
 * 
 * Comparaison avec la solution du stagiaire :
 * 
 * ❌ AVANT (stagiaire) :
 *    PaiementEspeces implements IModePaiement {
 *        void rembourser(...) {
 *            throw new UnsupportedOperationException("impossible");
 *        }
 *    }
 *    → Violation de LSP : exception à l'exécution
 * 
 * ✅ APRÈS (solution correcte) :
 *    PaiementEspeces implements IModePaiement (SANS IRemboursable)
 *    → Respect de LSP : erreur de compilation si on tente de rembourser
 */
@Component
public class PaiementEspeces implements IModePaiement {

    @Override
    public Paiement payer(BigDecimal montant) {
        // Simulation du paiement en espèces
        System.out.println("[ESPECES] Encaissement en espèces à l'agence : " + montant + " €");
        
        Paiement paiement = new Paiement();
        paiement.setMontant(montant);
        paiement.setDatePaiement(LocalDate.now());
        paiement.setModePaiement(ModePaiement.ESPECES);
        
        System.out.println("[ESPECES] Paiement encaissé avec succès");
        return paiement;
    }

    @Override
    public ModePaiement getType() {
        return ModePaiement.ESPECES;
    }
    
    /**
     * ⚠️ ATTENTION : Cette classe N'implémente PAS IRemboursable
     * 
     * Il est donc IMPOSSIBLE d'appeler une méthode rembourser() sur cette classe.
     * Le compilateur Java empêche ce genre d'erreur.
     * 
     * Exemple de code qui NE COMPILE PAS :
     * 
     *     PaiementEspeces especes = new PaiementEspeces();
     *     especes.rembourser(paiement);  // ❌ ERREUR DE COMPILATION
     * 
     * Seul le code suivant compile :
     * 
     *     if (modePaiement instanceof IRemboursable remboursable) {
     *         remboursable.rembourser(paiement);  // ✅ OK
     *     } else {
     *         // Gestion du cas non-remboursable
     *     }
     */
}
