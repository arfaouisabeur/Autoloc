package tn.esprit.sabeurarfaoui4cce11.service;

import tn.esprit.sabeurarfaoui4cce11.domain.Vehicule;

import java.util.List;

public interface IVehiculeService {

    Vehicule create(Vehicule vehicule);

    Vehicule findById(Long id);

    List<Vehicule> findAll();

    Vehicule update(Long id, Vehicule vehicule);

    void deleteById(Long id);
}
