package br.com.agendajulyana.pagamento.integration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class MercadoPagoWebhookValidator {
    private final MercadoPagoProperties properties;

    public MercadoPagoWebhookValidator(MercadoPagoProperties properties) {
        this.properties = properties;
    }

    public boolean isValid(String signature, String requestId, String dataId) {
        if (signature == null || requestId == null || dataId == null
                || properties.webhookSecret() == null || properties.webhookSecret().isBlank()) {
            return false;
        }

        Map<String, String> parts = new HashMap<>();
        for (String part : signature.split(",")) {
            var pieces = part.trim().split("=", 2);
            if (pieces.length == 2) parts.put(pieces[0], pieces[1]);
        }

        var ts = parts.get("ts");
        var v1 = parts.get("v1");
        if (ts == null || v1 == null) return false;

        try {
            var manifest = "id:" + dataId.toLowerCase() + ";request-id:" + requestId + ";ts:" + ts + ";";
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.webhookSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            var expected = java.util.HexFormat.of().formatHex(mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.US_ASCII),
                    v1.getBytes(StandardCharsets.US_ASCII)
            );
        } catch (Exception e) {
            return false;
        }
    }
}
