package br.com.agendajulyana.auth.dto;

public record VerifyResetCodeResponse(String tokenRedefinicao, long expiraEmSegundos) {}
