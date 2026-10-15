package tn.esprit.sabeurarfaoui4cce11.service;

import tn.esprit.sabeurarfaoui4cce11.domain.Contrat;

import java.util.List;

public interface IContratService {

    Contrat create(Contrat contrat);

    Contrat findById(Long id);

    List<Contrat> findAll();

    Contrat update(Long id, Contrat contrat);

    void deleteById(Long id);
}
