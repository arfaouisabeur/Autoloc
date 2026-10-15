package tn.esprit.sabeurarfaoui4cce11.service;

import tn.esprit.sabeurarfaoui4cce11.domain.Maintenance;

import java.util.List;

public interface IMaintenanceService {

    Maintenance create(Maintenance maintenance);

    Maintenance findById(Long id);

    List<Maintenance> findAll();

    Maintenance update(Long id, Maintenance maintenance);

    void deleteById(Long id);
}
