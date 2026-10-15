package tn.esprit.sabeurarfaoui4cce11.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.sabeurarfaoui4cce11.domain.Maintenance;
import tn.esprit.sabeurarfaoui4cce11.exception.ResourceNotFoundException;
import tn.esprit.sabeurarfaoui4cce11.repository.IMaintenanceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements IMaintenanceService {

    private final IMaintenanceRepository maintenanceRepository;

    @Override
    public Maintenance create(Maintenance maintenance) {
        if (maintenance.getIdMaintenance() != null) {
            throw new IllegalArgumentException(
                    "Une nouvelle maintenance ne doit pas avoir d'identifiant");
        }
        if (maintenance.getDateDebut() != null && maintenance.getDateFin() != null) {
            if (maintenance.getDateFin().isBefore(maintenance.getDateDebut())) {
                throw new IllegalArgumentException(
                        "La date de fin ne peut pas être antérieure à la date de début");
            }
        }
        return maintenanceRepository.save(maintenance);
    }

    @Override
    public Maintenance findById(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance", id));
    }

    @Override
    public List<Maintenance> findAll() {
        return maintenanceRepository.findAll();
    }

    @Override
    public Maintenance update(Long id, Maintenance maintenance) {
        Maintenance existant = findById(id);
        
        if (maintenance.getDateDebut() != null) {
            existant.setDateDebut(maintenance.getDateDebut());
        }
        if (maintenance.getDateFin() != null) {
            existant.setDateFin(maintenance.getDateFin());
        }
        
        if (existant.getDateDebut() != null && existant.getDateFin() != null) {
            if (existant.getDateFin().isBefore(existant.getDateDebut())) {
                throw new IllegalArgumentException(
                        "La date de fin ne peut pas être antérieure à la date de début");
            }
        }
        
        if (maintenance.getDescription() != null) {
            existant.setDescription(maintenance.getDescription());
        }
        
        return maintenanceRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!maintenanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Maintenance", id);
        }
        maintenanceRepository.deleteById(id);
    }
}
