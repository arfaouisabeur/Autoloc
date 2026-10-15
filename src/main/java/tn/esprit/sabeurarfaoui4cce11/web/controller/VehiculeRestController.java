package tn.esprit.sabeurarfaoui4cce11.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import tn.esprit.sabeurarfaoui4cce11.domain.Vehicule;
import tn.esprit.sabeurarfaoui4cce11.service.impls.VehiculeServicesImpl;

import java.util.List;

@RestController
@RequestMapping("vehicules")
@RequiredArgsConstructor
public class VehiculeRestController {

    private final VehiculeServicesImpl vehiculeServices;

    @PostMapping
    public Vehicule create(@RequestBody Vehicule vehicule) {
        return vehiculeServices.create(vehicule);
    }

    @GetMapping
    public List<Vehicule> findAll() {
        return vehiculeServices.findAll();
    }

    @GetMapping("/{id}")
    public Vehicule findById(@PathVariable Long id) {
        return vehiculeServices.findById(id);
    }

    @PutMapping("/{id}")
    public Vehicule update(
            @PathVariable Long id,
            @RequestBody Vehicule vehicule) {
        return vehiculeServices.update(id, vehicule);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        vehiculeServices.deleteById(id);
    }
}
