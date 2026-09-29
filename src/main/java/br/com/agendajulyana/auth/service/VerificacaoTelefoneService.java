package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.auth.domain.Usuario;
import br.com.agendajulyana.auth.domain.VerificacaoTelefone;
import br.com.agendajulyana.auth.dto.MeResponse;
import br.com.agendajulyana.auth.repository.UsuarioRepository;
import br.com.agendajulyana.auth.repository.VerificacaoTelefoneRepository;
import br.com.agendajulyana.integration.whatsapp.WhatsAppMessageSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.stream.Collectors;

@Service
public class VerificacaoTelefoneService {
    private static final int CODE_EXPIRATION_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private final UsuarioRepository usuarios;
    private final VerificacaoTelefoneRepository verificacoes;
    private final WhatsAppMessageSender whatsapp;
    private final SecureRandom random = new SecureRandom();
    private final String templateName;
    private final String templateLanguage;

    public VerificacaoTelefoneService(
            UsuarioRepository usuarios,
            VerificacaoTelefoneRepository verificacoes,
            WhatsAppMessageSender whatsapp
    ) {
        this.usuarios = usuarios;
        this.verificacoes = verificacoes;
        this.whatsapp = whatsapp;
        this.templateName = templateName;
        this.templateLanguage = templateLanguage;
    }

    @Transactional
    public void solicitar(String email, String telefone) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        String novoTelefone = normalizarTelefone(telefone);
        if (usuario.isTelefoneVerificado() && novoTelefone.equals(usuario.getTelefone())) {
            throw new IllegalStateException("Este telefone já está verificado.");
        }

        var agora = OffsetDateTime.now(ZoneOffset.UTC);
        verificacoes.findTopByUsuarioIdAndUtilizadoEmIsNullOrderByCriadoEmDesc(usuario.getId())
                .filter(v -> v.getCriadoEm().plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(agora))
                .ifPresent(v -> { throw new IllegalStateException("Aguarde antes de solicitar um novo código."); });

        usuario.atualizarDados(null, novoTelefone);
        usuarios.save(usuario);

        verificacoes.findTopByUsuarioIdAndUtilizadoEmIsNullOrderByCriadoEmDesc(usuario.getId())
                .ifPresent(VerificacaoTelefone::invalidar);

        String codigo = gerarCodigo();
        var verificacao = new VerificacaoTelefone(
                usuario,
                hash(codigo),
                agora.plusMinutes(CODE_EXPIRATION_MINUTES)
        );
        verificacoes.save(verificacao);

        whatsapp.sendVerificationCode(novoTelefone, codigo);
    }

    @Transactional
    public MeResponse confirmar(String email, String codigo) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        var verificacao = verificacoes.findTopByUsuarioIdAndUtilizadoEmIsNullOrderByCriadoEmDesc(usuario.getId())
                .orElseThrow(() -> new IllegalStateException("Nenhum código de verificação ativo."));

        var agora = OffsetDateTime.now(ZoneOffset.UTC);
        if (verificacao.getExpiraEm().isBefore(agora)) {
            verificacao.invalidar();
            throw new IllegalStateException("Código de verificação expirado.");
        }

        if (verificacao.getTentativas() >= MAX_ATTEMPTS) {
            verificacao.invalidar();
            throw new IllegalStateException("Limite de tentativas excedido.");
        }

        verificacao.registrarTentativa();
        if (!hash(codigo).equals(verificacao.getCodigoHash())) {
            throw new IllegalStateException("Código de verificação inválido.");
        }

        verificacao.confirmar();
        usuario.confirmarTelefone();
        usuarios.save(usuario);

        return new MeResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getStatus().name(),
                usuario.getPapeis().stream()
                        .map(p -> p.getNome().name())
                        .collect(Collectors.toUnmodifiableSet()),
                usuario.isTelefoneVerificado()
        );
    }

    private String gerarCodigo() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private String normalizarTelefone(String telefone) {
        String normalized = telefone.replaceAll("\\D", "");
        if (normalized.length() < 10 || normalized.length() > 15) {
            throw new IllegalArgumentException("Telefone inválido.");
        }
        return normalized;
    }

    private String hash(String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível processar o código de verificação.", e);
        }
    }
}