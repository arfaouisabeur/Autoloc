package tn.esprit.sabeurarfaoui4cce11.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.sabeurarfaoui4cce11.domain.Reservation;
import tn.esprit.sabeurarfaoui4cce11.exception.ResourceNotFoundException;
import tn.esprit.sabeurarfaoui4cce11.repository.IReservationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements IReservationService {

    private final IReservationRepository reservationRepository;

    @Override
    public Reservation create(Reservation reservation) {
        if (reservation.getIdReservation() != null) {
            throw new IllegalArgumentException(
                    "Une nouvelle réservation ne doit pas avoir d'identifiant");
        }
        if (reservation.getDateDebut() != null && reservation.getDateFin() != null) {
            if (reservation.getDateFin().isBefore(reservation.getDateDebut())) {
                throw new IllegalArgumentException(
                        "La date de fin ne peut pas être antérieure à la date de début");
            }
        }
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    @Override
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation update(Long id, Reservation reservation) {
        Reservation existant = findById(id);
        
        if (reservation.getDateDebut() != null) {
            existant.setDateDebut(reservation.getDateDebut());
        }
        if (reservation.getDateFin() != null) {
            existant.setDateFin(reservation.getDateFin());
        }
        
        if (existant.getDateDebut() != null && existant.getDateFin() != null) {
            if (existant.getDateFin().isBefore(existant.getDateDebut())) {
                throw new IllegalArgumentException(
                        "La date de fin ne peut pas être antérieure à la date de début");
            }
        }
        
        if (reservation.getStatut() != null) {
            existant.setStatut(reservation.getStatut());
        }
        
        return reservationRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation", id);
        }
        reservationRepository.deleteById(id);
    }
}
