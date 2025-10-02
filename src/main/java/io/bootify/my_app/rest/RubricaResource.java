package io.bootify.my_app.rest;

import io.bootify.my_app.model.RubricaDTO;
import io.bootify.my_app.service.RubricaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/rubricas", produces = MediaType.APPLICATION_JSON_VALUE)
public class RubricaResource {

    private final RubricaService rubricaService;

    public RubricaResource(final RubricaService rubricaService) {
        this.rubricaService = rubricaService;
    }

    @GetMapping
    public ResponseEntity<List<RubricaDTO>> getAllRubricas() {
        return ResponseEntity.ok(rubricaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RubricaDTO> getRubrica(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(rubricaService.get(id));
    }

    @PostMapping
    public ResponseEntity<Long> createRubrica(@RequestBody @Valid final RubricaDTO rubricaDTO) {
        final Long createdId = rubricaService.create(rubricaDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateRubrica(@PathVariable(name = "id") final Long id,
            @RequestBody @Valid final RubricaDTO rubricaDTO) {
        rubricaService.update(id, rubricaDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRubrica(@PathVariable(name = "id") final Long id) {
        rubricaService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
