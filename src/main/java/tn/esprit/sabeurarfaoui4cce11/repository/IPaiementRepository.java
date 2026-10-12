package tn.esprit.sabeurarfaoui4cce11.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.sabeurarfaoui4cce11.domain.Paiement;

public interface IPaiementRepository extends JpaRepository<Paiement, Long> {
}
