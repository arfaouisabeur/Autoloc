package tn.esprit.sabeurarfaoui4cce11.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.sabeurarfaoui4cce11.domain.Agence;
import tn.esprit.sabeurarfaoui4cce11.service.IAgenceService;

import java.util.List;

@RestController
@RequestMapping("agences")
@RequiredArgsConstructor
public class AgenceRestController {

    private final IAgenceService agenceService;

    @PostMapping
    public Agence create(@RequestBody Agence agence) {
        return agenceService.create(agence);
    }

    @GetMapping
    public List<Agence> findAll() {
        return agenceService.findAll();
    }

    @GetMapping("/{id}")
    public Agence findById(@PathVariable Long id) {
        return agenceService.findById(id);
    }

    @PutMapping("/{id}")
    public Agence update(
            @PathVariable Long id,
            @RequestBody Agence agence) {
        return agenceService.update(id, agence);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        agenceService.deleteById(id);
    }
}
