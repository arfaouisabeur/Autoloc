package tn.esprit.sabeurarfaoui4cce11.service.paiement;

import org.springframework.stereotype.Component;
import tn.esprit.sabeurarfaoui4cce11.domain.ModePaiement;
import tn.esprit.sabeurarfaoui4cce11.domain.Paiement;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Implémentation du mode de paiement par CARTE.
 * 
 * Cette classe implémente les deux interfaces :
 * - IModePaiement : car on peut payer par carte
 * - IRemboursable : car on peut rembourser un paiement par carte
 * 
 * PRINCIPE LSP RESPECTÉ : Cette classe peut être utilisée partout où
 * IModePaiement ou IRemboursable est attendu, sans violer leurs contrats.
 */
@Component
public class PaiementCarte implements IModePaiement, IRemboursable {

    @Override
    public Paiement payer(BigDecimal montant) {
        // Simulation du paiement par carte bancaire
        System.out.println("[CARTE] Débit de la carte bancaire : " + montant + " €");
        
        // Appliquer les frais de transaction (1%)
        BigDecimal montantAvecFrais = montant.multiply(BigDecimal.valueOf(1.01));
        
        Paiement paiement = new Paiement();
        paiement.setMontant(montantAvecFrais);
        paiement.setDatePaiement(LocalDate.now());
        paiement.setModePaiement(ModePaiement.CARTE);
        
        System.out.println("[CARTE] Paiement effectué avec succès (montant final : " + montantAvecFrais + " €)");
        return paiement;
    }

    @Override
    public void rembourser(Paiement paiement) {
        if (paiement == null || paiement.getMontant() == null) {
            throw new IllegalArgumentException("Paiement invalide pour le remboursement");
        }
        
        if (paiement.getModePaiement() != ModePaiement.CARTE) {
            throw new IllegalArgumentException("Ce paiement n'est pas un paiement par carte");
        }
        
        // Simulation du remboursement sur la carte
        System.out.println("[CARTE] Remboursement de " + paiement.getMontant() + " € sur la carte bancaire");
        System.out.println("[CARTE] Le montant sera crédité sous 3-5 jours ouvrables");
    }

    @Override
    public ModePaiement getType() {
        return ModePaiement.CARTE;
    }
}
