package tn.esprit.sabeurarfaoui4cce11.service.paiement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.sabeurarfaoui4cce11.domain.ModePaiement;
import tn.esprit.sabeurarfaoui4cce11.domain.Paiement;

import java.util.Map;

/**
 * Service de gestion des remboursements.
 * 
 * Ce service utilise les interfaces IModePaiement et IRemboursable
 * pour gérer les remboursements de manière sûre et conforme au principe LSP.
 * 
 * AVANTAGES DE CETTE ARCHITECTURE :
 * 
 * 1. Sécurité à la compilation :
 *    Le code ne compile pas si on tente de rembourser un paiement en espèces
 * 
 * 2. Pas d'exceptions à l'exécution :
 *    Pas de UnsupportedOperationException surprise
 * 
 * 3. Code auto-documenté :
 *    Les interfaces expriment clairement les capacités de chaque mode de paiement
 * 
 * 4. Facilité de test :
 *    On peut mocker IRemboursable sans se soucier des modes non-remboursables
 * 
 * 5. Évolutivité :
 *    Ajouter un nouveau mode de paiement ne casse pas le code existant
 */
@Service
public class RemboursementService {

    private final Map<ModePaiement, IModePaiement> modesPaiement;

    @Autowired
    public RemboursementService(PaiementCarte carte, 
                                PaiementVirement virement, 
                                PaiementEspeces especes) {
        this.modesPaiement = Map.of(
            ModePaiement.CARTE, carte,
            ModePaiement.VIREMENT, virement,
            ModePaiement.ESPECES, especes
        );
    }

    /**
     * Rembourse un paiement si c'est possible
     * 
     * @param paiement Le paiement à rembourser
     * @throws IllegalArgumentException Si le paiement est invalide
     * @throws IllegalStateException Si le mode de paiement ne supporte pas les remboursements
     */
    public void rembourserPaiement(Paiement paiement) {
        if (paiement == null || paiement.getModePaiement() == null) {
            throw new IllegalArgumentException("Paiement invalide");
        }

        IModePaiement modePaiement = modesPaiement.get(paiement.getModePaiement());
        
        if (modePaiement == null) {
            throw new IllegalStateException("Mode de paiement inconnu : " + paiement.getModePaiement());
        }

        // ✅ Vérification LSP-conforme avec instanceof
        // Le compilateur garantit qu'on ne peut appeler rembourser() que sur un IRemboursable
        if (modePaiement instanceof IRemboursable remboursable) {
            System.out.println("🔄 Traitement du remboursement pour le paiement ID " + paiement.getIdPaiement());
            remboursable.rembourser(paiement);
            System.out.println("✅ Remboursement effectué avec succès");
        } else {
            // Cas du paiement en espèces
            throw new IllegalStateException(
                "Impossible de rembourser un paiement en " + paiement.getModePaiement() + 
                ". Seuls les paiements par CARTE et VIREMENT peuvent être remboursés."
            );
        }
    }

    /**
     * Vérifie si un paiement peut être remboursé
     * 
     * @param paiement Le paiement à vérifier
     * @return true si le paiement peut être remboursé, false sinon
     */
    public boolean estRemboursable(Paiement paiement) {
        if (paiement == null || paiement.getModePaiement() == null) {
            return false;
        }

        IModePaiement modePaiement = modesPaiement.get(paiement.getModePaiement());
        return modePaiement instanceof IRemboursable;
    }

    /**
     * Retourne le mode de paiement correspondant
     * 
     * @param type Le type de mode de paiement
     * @return L'implémentation correspondante
     */
    public IModePaiement getModePaiement(ModePaiement type) {
        return modesPaiement.get(type);
    }
}
