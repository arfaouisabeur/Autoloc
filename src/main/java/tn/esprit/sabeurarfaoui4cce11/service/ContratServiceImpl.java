package tn.esprit.sabeurarfaoui4cce11.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.sabeurarfaoui4cce11.domain.Contrat;
import tn.esprit.sabeurarfaoui4cce11.exception.ResourceNotFoundException;
import tn.esprit.sabeurarfaoui4cce11.repository.IContratRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContratServiceImpl implements IContratService {

    private final IContratRepository contratRepository;

    @Override
    public Contrat create(Contrat contrat) {
        if (contrat.getIdContrat() != null) {
            throw new IllegalArgumentException(
                    "Un nouveau contrat ne doit pas avoir d'identifiant");
        }
        if (contrat.getMontantTotal() != null && contrat.getMontantTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Le montant total ne peut pas être négatif");
        }
        return contratRepository.save(contrat);
    }

    @Override
    public Contrat findById(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", id));
    }

    @Override
    public List<Contrat> findAll() {
        return contratRepository.findAll();
    }

    @Override
    public Contrat update(Long id, Contrat contrat) {
        Contrat existant = findById(id);
        
        if (contrat.getMontantTotal() != null) {
            if (contrat.getMontantTotal().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Le montant total ne peut pas être négatif");
            }
            existant.setMontantTotal(contrat.getMontantTotal());
        }
        if (contrat.getDateSignature() != null) {
            existant.setDateSignature(contrat.getDateSignature());
        }
        existant.setValide(contrat.isValide());
        
        return contratRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!contratRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contrat", id);
        }
        contratRepository.deleteById(id);
    }
}
