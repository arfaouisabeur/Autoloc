package tn.esprit.sabeurarfaoui4cce11.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.sabeurarfaoui4cce11.domain.Reservation;

public interface IReservationRepository extends JpaRepository<Reservation, Long> {
}
