package br.com.agendajulyana.auth.dto;
import java.util.*;
public record MeResponse(UUID usuarioId,String nome,String email,String telefone,String status,Set<String> papeis){}
