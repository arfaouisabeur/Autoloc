package tn.esprit.sabeurarfaoui4cce11.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.sabeurarfaoui4cce11.domain.Vehicule;
import tn.esprit.sabeurarfaoui4cce11.service.impls.VehiculeServicesImpl;

import java.util.List;

@RestController
@RequestMapping("vehicules")
@RequiredArgsConstructor
@Tag(name = "Vehicules", description = "Gestion des véhicules")
public class VehiculeRestController {

    private final VehiculeServicesImpl vehiculeServices;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un véhicule")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Véhicule créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public Vehicule create(@RequestBody Vehicule vehicule) {
        return vehiculeServices.create(vehicule);
    }

    @GetMapping
    @Operation(summary = "Lister tous les véhicules")
    @ApiResponse(responseCode = "200", description = "Liste des véhicules")
    public List<Vehicule> findAll() {
        return vehiculeServices.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un véhicule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Véhicule trouvé"),
            @ApiResponse(responseCode = "404", description = "Véhicule introuvable")
    })
    public Vehicule findById(
            @Parameter(description = "Identifiant du véhicule")
            @PathVariable Long id) {
        return vehiculeServices.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un véhicule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Véhicule modifié"),
            @ApiResponse(responseCode = "404", description = "Véhicule introuvable"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public Vehicule update(
            @Parameter(description = "Identifiant du véhicule")
            @PathVariable Long id,
            @RequestBody Vehicule vehicule) {
        return vehiculeServices.update(id, vehicule);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un véhicule")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Véhicule supprimé"),
            @ApiResponse(responseCode = "404", description = "Véhicule introuvable")
    })
    public void deleteById(
            @Parameter(description = "Identifiant du véhicule")
            @PathVariable Long id) {
        vehiculeServices.deleteById(id);
    }
}
