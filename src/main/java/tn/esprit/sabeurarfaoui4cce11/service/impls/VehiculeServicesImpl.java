package tn.esprit.sabeurarfaoui4cce11.service.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.sabeurarfaoui4cce11.domain.Vehicule;
import tn.esprit.sabeurarfaoui4cce11.exception.ResourceNotFoundException;
import tn.esprit.sabeurarfaoui4cce11.repository.IVehiculeRepository;
import tn.esprit.sabeurarfaoui4cce11.service.IVehiculeService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehiculeServicesImpl implements IVehiculeService {

    private final IVehiculeRepository vehiculeRepository;

    @Override
    public Vehicule create(Vehicule vehicule) {
        if (vehicule.getIdVehicule() != null) {
            throw new IllegalArgumentException(
                    "Un nouveau véhicule ne doit pas avoir d'identifiant");
        }
        if (vehicule.getTarifJournalier() != null && 
            vehicule.getTarifJournalier().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Le tarif journalier ne peut pas être négatif");
        }
        return vehiculeRepository.save(vehicule);
    }

    @Override
    public Vehicule findById(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id));
    }

    @Override
    public List<Vehicule> findAll() {
        return vehiculeRepository.findAll();
    }

    @Override
    public Vehicule update(Long id, Vehicule vehicule) {
        Vehicule existant = findById(id);
        
        if (vehicule.getImmatriculation() != null) {
            existant.setImmatriculation(vehicule.getImmatriculation());
        }
        if (vehicule.getMarque() != null) {
            existant.setMarque(vehicule.getMarque());
        }
        if (vehicule.getModele() != null) {
            existant.setModele(vehicule.getModele());
        }
        if (vehicule.getCategorie() != null) {
            existant.setCategorie(vehicule.getCategorie());
        }
        if (vehicule.getTarifJournalier() != null) {
            if (vehicule.getTarifJournalier().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Le tarif journalier ne peut pas être négatif");
            }
            existant.setTarifJournalier(vehicule.getTarifJournalier());
        }
        if (vehicule.getStatut() != null) {
            existant.setStatut(vehicule.getStatut());
        }
        
        return vehiculeRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!vehiculeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicule", id);
        }
        vehiculeRepository.deleteById(id);
    }
}
