package tn.esprit.sabeurarfaoui4cce11.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.sabeurarfaoui4cce11.domain.Vehicule;

@Repository
public interface VehiculeRepository extends CrudRepository <Vehicule, Long> {
}
