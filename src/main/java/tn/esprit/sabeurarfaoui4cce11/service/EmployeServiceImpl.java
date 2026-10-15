package tn.esprit.sabeurarfaoui4cce11.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.sabeurarfaoui4cce11.domain.Employe;
import tn.esprit.sabeurarfaoui4cce11.exception.ResourceNotFoundException;
import tn.esprit.sabeurarfaoui4cce11.repository.IEmployeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeServiceImpl implements IEmployeService {

    private final IEmployeRepository employeRepository;

    @Override
    public Employe create(Employe employe) {
        if (employe.getIdEmploye() != null) {
            throw new IllegalArgumentException(
                    "Un nouvel employé ne doit pas avoir d'identifiant");
        }
        if (employe.getNom() == null || employe.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Le nom de l'employé est obligatoire");
        }
        if (employe.getRole() == null) {
            throw new IllegalArgumentException(
                    "Le rôle de l'employé est obligatoire");
        }
        return employeRepository.save(employe);
    }

    @Override
    public Employe findById(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", id));
    }

    @Override
    public List<Employe> findAll() {
        return employeRepository.findAll();
    }

    @Override
    public Employe update(Long id, Employe employe) {
        Employe existant = findById(id);
        
        if (employe.getNom() != null) {
            if (employe.getNom().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Le nom de l'employé ne peut pas être vide");
            }
            existant.setNom(employe.getNom());
        }
        if (employe.getPrenom() != null) {
            existant.setPrenom(employe.getPrenom());
        }
        if (employe.getRole() != null) {
            existant.setRole(employe.getRole());
        }
        
        return employeRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!employeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employe", id);
        }
        employeRepository.deleteById(id);
    }
}
