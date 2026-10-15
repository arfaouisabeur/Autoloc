package tn.esprit.sabeurarfaoui4cce11.service;

import tn.esprit.sabeurarfaoui4cce11.domain.Paiement;

import java.util.List;

public interface IPaiementService {

    Paiement findById(Long id);

    List<Paiement> findAll();
}
