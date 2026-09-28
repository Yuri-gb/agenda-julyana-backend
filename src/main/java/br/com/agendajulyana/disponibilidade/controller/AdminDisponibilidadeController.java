package br.com.agendajulyana.disponibilidade.controller;

import br.com.agendajulyana.disponibilidade.dto.*;
import br.com.agendajulyana.auth.repository.UsuarioRepository;
import br.com.agendajulyana.disponibilidade.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/disponibilidades")
@Tag(name="Admin — Disponibilidade", description="Configuração de disponibilidade, bloqueios e indisponibilidades de serviços.")
@SecurityRequirement(name="bearerAuth")
public class AdminDisponibilidadeController {
    private final DisponibilidadeService disponibilidade; private final BloqueioService bloqueio; private final IndisponibilidadeServicoService indisponibilidade; private final UsuarioRepository usuarios;
    public AdminDisponibilidadeController(DisponibilidadeService d,BloqueioService b,IndisponibilidadeServicoService i,UsuarioRepository u){disponibilidade=d;bloqueio=b;indisponibilidade=i;usuarios=u;}
    private UUID usuarioId(Authentication a){return usuarios.findByEmailIgnoreCase(a.getName()).orElseThrow(()->new IllegalArgumentException("Usuário autenticado não encontrado.")).getId();}
    @PostMapping @Operation(summary="Criar disponibilidade") @ResponseStatus(HttpStatus.CREATED) public DisponibilidadeResponse criar(@Valid @RequestBody DisponibilidadeRequest r, Authentication a){return disponibilidade.criar(r, usuarioId(a));}
    @GetMapping @Operation(summary="Listar disponibilidades") public List<DisponibilidadeResponse> listar(){return disponibilidade.listar();}
    @GetMapping("/{id}") @Operation(summary="Consultar disponibilidade") public DisponibilidadeResponse buscar(@PathVariable UUID id){return disponibilidade.buscar(id);}
    @PutMapping("/{id}") @Operation(summary="Atualizar disponibilidade") public DisponibilidadeResponse atualizar(@PathVariable UUID id,@Valid @RequestBody DisponibilidadeRequest r, Authentication a){return disponibilidade.atualizar(id,r, UUID.fromString(a.getName()));}
    @PatchMapping("/{id}/status") @Operation(summary="Alterar status da disponibilidade") public DisponibilidadeResponse status(@PathVariable UUID id,@Valid @RequestBody DisponibilidadeStatusRequest r, Authentication a){return disponibilidade.alterarStatus(id,r, UUID.fromString(a.getName()));}

    @PostMapping("/bloqueios") @Operation(summary="Criar bloqueio") @ResponseStatus(HttpStatus.CREATED) public BloqueioResponse criarBloqueio(@Valid @RequestBody BloqueioRequest r,Authentication a){return bloqueio.criar(r,UUID.fromString(a.getName()));}
    @GetMapping("/bloqueios") @Operation(summary="Listar bloqueios") public List<BloqueioResponse> listarBloqueios(){return bloqueio.listar();}
    @GetMapping("/bloqueios/{id}") @Operation(summary="Consultar bloqueio") public BloqueioResponse buscarBloqueio(@PathVariable UUID id){return bloqueio.buscar(id);}
    @PutMapping("/bloqueios/{id}") @Operation(summary="Atualizar bloqueio") public BloqueioResponse atualizarBloqueio(@PathVariable UUID id,@Valid @RequestBody BloqueioRequest r, Authentication a){return bloqueio.atualizar(id,r, UUID.fromString(a.getName()));}
    @DeleteMapping("/bloqueios/{id}") @Operation(summary="Excluir bloqueio") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluirBloqueio(@PathVariable UUID id, Authentication a){bloqueio.excluir(id, UUID.fromString(a.getName()));}

    @PostMapping("/servicos-indisponiveis") @Operation(summary="Criar indisponibilidade de serviço") @ResponseStatus(HttpStatus.CREATED) public IndisponibilidadeServicoResponse criarInd(@Valid @RequestBody IndisponibilidadeServicoRequest r){return indisponibilidade.criar(r);}
    @GetMapping("/servicos-indisponiveis") @Operation(summary="Listar indisponibilidades de serviço") public List<IndisponibilidadeServicoResponse> listarInd(){return indisponibilidade.listar();}
    @GetMapping("/servicos-indisponiveis/{id}") @Operation(summary="Consultar indisponibilidade de serviço") public IndisponibilidadeServicoResponse buscarInd(@PathVariable UUID id){return indisponibilidade.buscar(id);}
    @PutMapping("/servicos-indisponiveis/{id}") @Operation(summary="Atualizar indisponibilidade de serviço") public IndisponibilidadeServicoResponse atualizarInd(@PathVariable UUID id,@Valid @RequestBody IndisponibilidadeServicoRequest r, Authentication a){return indisponibilidade.atualizar(id,r, UUID.fromString(a.getName()));}
    @DeleteMapping("/servicos-indisponiveis/{id}") @Operation(summary="Excluir indisponibilidade de serviço") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluirInd(@PathVariable UUID id, Authentication a){indisponibilidade.excluir(id, UUID.fromString(a.getName()));}
}