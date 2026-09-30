package tn.esprit.sabeurarfaoui4cce11;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tn.esprit.sabeurarfaoui4cce11.domain.CategorieVehicule;
import tn.esprit.sabeurarfaoui4cce11.domain.StatutVehicule;
import tn.esprit.sabeurarfaoui4cce11.domain.Vehicule;
import tn.esprit.sabeurarfaoui4cce11.repository.VehiculeRepository;

import java.math.BigDecimal;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final VehiculeRepository vehiculeRepository;

    @Override
    public void run(String... args) {
        if (vehiculeRepository.count() == 0) {

            Vehicule v1 = new Vehicule(null, "TUN-123-AB", "Mercedes", "G Class",
                    CategorieVehicule.SUV, new BigDecimal("350.00"), StatutVehicule.DISPONIBLE);

            Vehicule v2 = new Vehicule(null, "TUN-456-CD", "Peugeot", "206",
                    CategorieVehicule.CITADINE, new BigDecimal("40.00"), StatutVehicule.DISPONIBLE);

            Vehicule v3 = new Vehicule(null, "TUN-789-EF", "Cadillac", "Escalade",
                    CategorieVehicule.SUV, new BigDecimal("280.00"), StatutVehicule.MAINTENANCE);

            vehiculeRepository.save(v1);
            vehiculeRepository.save(v2);
            vehiculeRepository.save(v3);

            log.info(">>> DataInitializer : 3 vehicules de demonstration inseres en base.");
        } else {
            log.info(">>> DataInitializer : des donnees existent deja, aucune insertion.");
        }
    }
}
