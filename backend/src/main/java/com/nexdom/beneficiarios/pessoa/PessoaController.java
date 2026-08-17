package com.nexdom.beneficiarios.pessoa;

import com.nexdom.beneficiarios.pessoa.dto.PessoaCreateRequest;
import com.nexdom.beneficiarios.pessoa.dto.PessoaResponse;
import com.nexdom.beneficiarios.pessoa.dto.PessoaUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/pessoas")
public class PessoaController {

    private final PessoaService pessoaService;

    public PessoaController(PessoaService pessoaService) {
        this.pessoaService = pessoaService;
    }

    @GetMapping
    public PagedModel<PessoaResponse> listar(@RequestParam(required = false) String nome,
                                              @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return new PagedModel<>(pessoaService.listar(nome, pageable));
    }

    @GetMapping("/{id}")
    public PessoaResponse buscarPorId(@PathVariable UUID id) {
        return pessoaService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<PessoaResponse> criar(@Valid @RequestBody PessoaCreateRequest request) {
        PessoaResponse pessoa = pessoaService.criar(request);
        return ResponseEntity.created(URI.create("/pessoas/" + pessoa.id())).body(pessoa);
    }

    @PutMapping("/{id}")
    public PessoaResponse atualizar(@PathVariable UUID id, @Valid @RequestBody PessoaUpdateRequest request) {
        return pessoaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        pessoaService.deletar(id);
    }
}
