package tn.esprit.sabeurarfaoui4cce11.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.sabeurarfaoui4cce11.domain.Agence;
import tn.esprit.sabeurarfaoui4cce11.exception.ResourceNotFoundException;
import tn.esprit.sabeurarfaoui4cce11.repository.IAgenceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgenceServiceImpl implements IAgenceService {

    private final IAgenceRepository agenceRepository;

    @Override
    public Agence create(Agence agence) {
        if (agence.getIdAgence() != null) {
            throw new IllegalArgumentException(
                    "Une nouvelle agence ne doit pas avoir d'identifiant");
        }
        if (agence.getNom() == null || agence.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Le nom de l'agence est obligatoire");
        }
        return agenceRepository.save(agence);
    }

    @Override
    public Agence findById(Long id) {
        return agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", id));
    }

    @Override
    public List<Agence> findAll() {
        return agenceRepository.findAll();
    }

    @Override
    public Agence update(Long id, Agence agence) {
        Agence existant = findById(id);
        
        if (agence.getNom() != null) {
            if (agence.getNom().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Le nom de l'agence ne peut pas être vide");
            }
            existant.setNom(agence.getNom());
        }
        if (agence.getVille() != null) {
            existant.setVille(agence.getVille());
        }
        if (agence.getAdresse() != null) {
            existant.setAdresse(agence.getAdresse());
        }
        if (agence.getTelephone() != null) {
            existant.setTelephone(agence.getTelephone());
        }
        
        return agenceRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!agenceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agence", id);
        }
        agenceRepository.deleteById(id);
    }
}
