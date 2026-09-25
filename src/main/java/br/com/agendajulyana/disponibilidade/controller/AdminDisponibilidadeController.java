package br.com.agendajulyana.disponibilidade.controller;

import br.com.agendajulyana.disponibilidade.dto.*;
import br.com.agendajulyana.disponibilidade.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/disponibilidades")
public class AdminDisponibilidadeController {
    private final DisponibilidadeService disponibilidade; private final BloqueioService bloqueio; private final IndisponibilidadeServicoService indisponibilidade;
    public AdminDisponibilidadeController(DisponibilidadeService d,BloqueioService b,IndisponibilidadeServicoService i){disponibilidade=d;bloqueio=b;indisponibilidade=i;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public DisponibilidadeResponse criar(@Valid @RequestBody DisponibilidadeRequest r){return disponibilidade.criar(r);}
    @GetMapping public List<DisponibilidadeResponse> listar(){return disponibilidade.listar();}
    @GetMapping("/{id}") public DisponibilidadeResponse buscar(@PathVariable UUID id){return disponibilidade.buscar(id);}
    @PutMapping("/{id}") public DisponibilidadeResponse atualizar(@PathVariable UUID id,@Valid @RequestBody DisponibilidadeRequest r){return disponibilidade.atualizar(id,r);}
    @PatchMapping("/{id}/status") public DisponibilidadeResponse status(@PathVariable UUID id,@Valid @RequestBody DisponibilidadeStatusRequest r){return disponibilidade.alterarStatus(id,r);}

    @PostMapping("/bloqueios") @ResponseStatus(HttpStatus.CREATED) public BloqueioResponse criarBloqueio(@Valid @RequestBody BloqueioRequest r,Authentication a){return bloqueio.criar(r,UUID.fromString(a.getName()));}
    @GetMapping("/bloqueios") public List<BloqueioResponse> listarBloqueios(){return bloqueio.listar();}
    @GetMapping("/bloqueios/{id}") public BloqueioResponse buscarBloqueio(@PathVariable UUID id){return bloqueio.buscar(id);}
    @PutMapping("/bloqueios/{id}") public BloqueioResponse atualizarBloqueio(@PathVariable UUID id,@Valid @RequestBody BloqueioRequest r){return bloqueio.atualizar(id,r);}
    @DeleteMapping("/bloqueios/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluirBloqueio(@PathVariable UUID id){bloqueio.excluir(id);}

    @PostMapping("/servicos-indisponiveis") @ResponseStatus(HttpStatus.CREATED) public IndisponibilidadeServicoResponse criarInd(@Valid @RequestBody IndisponibilidadeServicoRequest r){return indisponibilidade.criar(r);}
    @GetMapping("/servicos-indisponiveis") public List<IndisponibilidadeServicoResponse> listarInd(){return indisponibilidade.listar();}
    @GetMapping("/servicos-indisponiveis/{id}") public IndisponibilidadeServicoResponse buscarInd(@PathVariable UUID id){return indisponibilidade.buscar(id);}
    @PutMapping("/servicos-indisponiveis/{id}") public IndisponibilidadeServicoResponse atualizarInd(@PathVariable UUID id,@Valid @RequestBody IndisponibilidadeServicoRequest r){return indisponibilidade.atualizar(id,r);}
    @DeleteMapping("/servicos-indisponiveis/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluirInd(@PathVariable UUID id){indisponibilidade.excluir(id);}
}