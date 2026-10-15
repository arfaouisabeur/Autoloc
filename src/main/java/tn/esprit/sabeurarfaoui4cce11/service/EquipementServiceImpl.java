package tn.esprit.sabeurarfaoui4cce11.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.sabeurarfaoui4cce11.domain.Equipement;
import tn.esprit.sabeurarfaoui4cce11.exception.ResourceNotFoundException;
import tn.esprit.sabeurarfaoui4cce11.repository.IEquipementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipementServiceImpl implements IEquipementService {

    private final IEquipementRepository equipementRepository;

    @Override
    public Equipement create(Equipement equipement) {
        if (equipement.getIdEquipement() != null) {
            throw new IllegalArgumentException(
                    "Un nouvel équipement ne doit pas avoir d'identifiant");
        }
        if (equipement.getLibelle() == null || equipement.getLibelle().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Le libellé de l'équipement est obligatoire");
        }
        return equipementRepository.save(equipement);
    }

    @Override
    public Equipement findById(Long id) {
        return equipementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipement", id));
    }

    @Override
    public List<Equipement> findAll() {
        return equipementRepository.findAll();
    }

    @Override
    public Equipement update(Long id, Equipement equipement) {
        Equipement existant = findById(id);
        
        if (equipement.getLibelle() != null) {
            if (equipement.getLibelle().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Le libellé de l'équipement ne peut pas être vide");
            }
            existant.setLibelle(equipement.getLibelle());
        }
        
        return equipementRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!equipementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipement", id);
        }
        equipementRepository.deleteById(id);
    }
}
