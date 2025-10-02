package io.bootify.my_app.service;

import io.bootify.my_app.domain.Rubrica;
import io.bootify.my_app.model.RubricaDTO;
import io.bootify.my_app.repos.RubricaRepository;
import io.bootify.my_app.util.NotFoundException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class RubricaService {

    private final RubricaRepository rubricaRepository;

    public RubricaService(final RubricaRepository rubricaRepository) {
        this.rubricaRepository = rubricaRepository;
    }

    public List<RubricaDTO> findAll() {
        final List<Rubrica> rubricas = rubricaRepository.findAll(Sort.by("id"));
        return rubricas.stream()
                .map(rubrica -> mapToDTO(rubrica, new RubricaDTO()))
                .toList();
    }

    public RubricaDTO get(final Long id) {
        return rubricaRepository.findById(id)
                .map(rubrica -> mapToDTO(rubrica, new RubricaDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final RubricaDTO rubricaDTO) {
        final Rubrica rubrica = new Rubrica();
        mapToEntity(rubricaDTO, rubrica);
        return rubricaRepository.save(rubrica).getId();
    }

    public void update(final Long id, final RubricaDTO rubricaDTO) {
        final Rubrica rubrica = rubricaRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(rubricaDTO, rubrica);
        rubricaRepository.save(rubrica);
    }

    public void delete(final Long id) {
        final Rubrica rubrica = rubricaRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        rubricaRepository.delete(rubrica);
    }

    private RubricaDTO mapToDTO(final Rubrica rubrica, final RubricaDTO rubricaDTO) {
        rubricaDTO.setId(rubrica.getId());
        rubricaDTO.setNome(rubrica.getNome());
        return rubricaDTO;
    }

    private Rubrica mapToEntity(final RubricaDTO rubricaDTO, final Rubrica rubrica) {
        rubrica.setNome(rubricaDTO.getNome());
        return rubrica;
    }

}
