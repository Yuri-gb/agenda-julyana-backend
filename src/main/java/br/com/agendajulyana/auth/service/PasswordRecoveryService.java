package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.auth.domain.ProvedorAutenticacao;
import br.com.agendajulyana.auth.domain.RecuperacaoSenha;
import br.com.agendajulyana.auth.dto.*;
import br.com.agendajulyana.auth.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class PasswordRecoveryService {
    private static final int CODIGO_EXPIRACAO_MINUTOS = 15;
    private static final int MAX_TENTATIVAS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarios;
    private final IdentidadeAutenticacaoRepository identidades;
    private final RecuperacaoSenhaRepository recuperacoes;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    public PasswordRecoveryService(
        UsuarioRepository usuarios,
        IdentidadeAutenticacaoRepository identidades,
        RecuperacaoSenhaRepository recuperacoes,
        PasswordEncoder encoder,
        EmailService emailService
    ) {
        this.usuarios = usuarios;
        this.identidades = identidades;
        this.recuperacoes = recuperacoes;
        this.encoder = encoder;
        this.emailService = emailService;
    }

    @Transactional
    public ForgotPasswordResponse solicitar(String email) {
        var emailNormalizado = email.trim().toLowerCase();

        var usuarioOpt = usuarios.findByEmailIgnoreCase(emailNormalizado);
        if (usuarioOpt.isEmpty()) {
            return respostaGenerica();
        }

        var usuario = usuarioOpt.get();

        if (!identidades.existsByUsuarioIdAndProvedor(usuario.getId(), ProvedorAutenticacao.LOCAL)) {
            return respostaGenerica();
        }

        var codigo = gerarCodigo();
        var recuperacao = new RecuperacaoSenha(
            usuario,
            encoder.encode(codigo),
            OffsetDateTime.now().plusMinutes(CODIGO_EXPIRACAO_MINUTOS)
        );

        recuperacoes.deleteByUsuarioIdAndCriadoEmBefore(
            usuario.getId(), OffsetDateTime.now().plusMinutes(1)
        );
        recuperacoes.save(recuperacao);

        emailService.enviarCodigoRecuperacao(usuario.getEmail(), usuario.getNome(), codigo);
        return respostaGenerica();
    }

    @Transactional
    public VerifyResetCodeResponse verificarCodigo(String email, String codigo) {
        var emailNormalizado = email.trim().toLowerCase();
        var usuario = usuarios.findByEmailIgnoreCase(emailNormalizado)
            .orElseThrow(() -> new IllegalArgumentException("Código inválido ou expirado."));

        var recuperacao = recuperacoes.findTopByUsuarioIdAndUtilizadoEmIsNullOrderByCriadoEmDesc(usuario.getId())
            .orElseThrow(() -> new IllegalArgumentException("Código inválido ou expirado."));

        if (recuperacao.getExpiraEm().isBefore(OffsetDateTime.now())
            || recuperacao.getTentativas() >= MAX_TENTATIVAS) {
            throw new IllegalArgumentException("Código inválido ou expirado.");
        }

        recuperacao.registrarTentativa();

        if (!encoder.matches(codigo, recuperacao.getCodigoHash())) {
            throw new IllegalArgumentException("Código inválido ou expirado.");
        }

        var token = UUID.randomUUID() + "-" + UUID.randomUUID();
        var tokenHash = sha256(token);
        recuperacao.verificar(tokenHash, OffsetDateTime.now().plusMinutes(10));

        return new VerifyResetCodeResponse(token, 600);
    }

    @Transactional
    public MessageResponse redefinirSenha(ResetPasswordRequest request) {
        if (!request.novaSenha().equals(request.confirmacaoSenha())) {
            throw new IllegalArgumentException("As senhas não conferem.");
        }

        var tokenHash = sha256(request.tokenRedefinicao());
        var recuperacao = recuperacoes.findByTokenRedefinicaoHashAndUtilizadoEmIsNull(tokenHash)
            .orElseThrow(() -> new IllegalArgumentException("Token de redefinição inválido ou expirado."));

        if (recuperacao.getTokenRedefinicaoExpiraEm() == null
            || recuperacao.getTokenRedefinicaoExpiraEm().isBefore(OffsetDateTime.now())
            || recuperacao.getVerificadoEm() == null) {
            throw new IllegalArgumentException("Token de redefinição inválido ou expirado.");
        }

        var usuario = recuperacao.getUsuario();
        var identidade = identidades.findByProvedorAndIdentificadorExterno(
                ProvedorAutenticacao.LOCAL, usuario.getEmail().toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException("Recuperação de senha indisponível."));

        identidade.atualizarSenhaHash(encoder.encode(request.novaSenha()));
        recuperacao.utilizar();
        identidades.save(identidade);

        return new MessageResponse("Senha redefinida com sucesso.");
    }

    private static String gerarCodigo() {
        return "%06d".formatted(RANDOM.nextInt(1_000_000));
    }

    private static String sha256(String valor) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível gerar o token de recuperação.", e);
        }
    }

    private static ForgotPasswordResponse respostaGenerica() {
        return new ForgotPasswordResponse(
            "Se o e-mail estiver cadastrado e possuir autenticação por senha, enviaremos as instruções de recuperação."
        );
    }
}
