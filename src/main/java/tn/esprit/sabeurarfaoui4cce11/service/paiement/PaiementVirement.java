package tn.esprit.sabeurarfaoui4cce11.service.paiement;

import org.springframework.stereotype.Component;
import tn.esprit.sabeurarfaoui4cce11.domain.ModePaiement;
import tn.esprit.sabeurarfaoui4cce11.domain.Paiement;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Implémentation du mode de paiement par VIREMENT bancaire.
 * 
 * Cette classe implémente les deux interfaces :
 * - IModePaiement : car on peut payer par virement
 * - IRemboursable : car on peut rembourser un paiement par virement
 * 
 * PRINCIPE LSP RESPECTÉ : Cette classe peut être utilisée partout où
 * IModePaiement ou IRemboursable est attendu, sans violer leurs contrats.
 */
@Component
public class PaiementVirement implements IModePaiement, IRemboursable {

    @Override
    public Paiement payer(BigDecimal montant) {
        // Simulation du paiement par virement bancaire
        System.out.println("[VIREMENT] Attente du virement bancaire : " + montant + " €");
        
        Paiement paiement = new Paiement();
        paiement.setMontant(montant);
        paiement.setDatePaiement(LocalDate.now());
        paiement.setModePaiement(ModePaiement.VIREMENT);
        
        System.out.println("[VIREMENT] Paiement enregistré (validation sous 1-2 jours ouvrables)");
        return paiement;
    }

    @Override
    public void rembourser(Paiement paiement) {
        if (paiement == null || paiement.getMontant() == null) {
            throw new IllegalArgumentException("Paiement invalide pour le remboursement");
        }
        
        if (paiement.getModePaiement() != ModePaiement.VIREMENT) {
            throw new IllegalArgumentException("Ce paiement n'est pas un paiement par virement");
        }
        
        // Simulation du remboursement par virement
        System.out.println("[VIREMENT] Remboursement de " + paiement.getMontant() + " € par virement bancaire");
        System.out.println("[VIREMENT] Le virement sera effectué sous 3-5 jours ouvrables");
    }

    @Override
    public ModePaiement getType() {
        return ModePaiement.VIREMENT;
    }
}
