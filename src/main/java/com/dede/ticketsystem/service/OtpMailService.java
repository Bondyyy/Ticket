package com.dede.ticketsystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OtpMailService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.mail.otp.enabled:true}")
    private boolean enabled;

    @Value("${app.mail.otp.script-url:}")
    private String scriptUrl;

    @Value("${app.mail.otp.secret:}")
    private String secret;

    public OtpMailService(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(8))
                .build();
        this.objectMapper = objectMapper;
    }

    public String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    public boolean sendOtpEmail(String to, String otp) {
        if (!enabled) {
            System.out.println("OTP đăng ký Dề Dê Tickets cho " + to + ": " + otp);
            return true;
        }

        if (scriptUrl == null || scriptUrl.isBlank() || secret == null || secret.isBlank()) {
            throw new RuntimeException("Chưa cấu hình app.mail.otp.script-url hoặc app.mail.otp.secret.");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("secret", secret);
        payload.put("to", to);
        payload.put("subject", "Mã OTP đăng ký Dề Dê Tickets");
        payload.put("otp", otp);
        payload.put("html", buildOtpHtml(otp));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(scriptUrl, request, String.class);
            JsonNode body = objectMapper.readTree(response.getBody());
            return response.getStatusCode().is2xxSuccessful() && body.path("ok").asBoolean(false);
        } catch (Exception ex) {
            throw new RuntimeException("Không gửi được email OTP. Vui lòng thử lại sau.");
        }
    }

    private String buildOtpHtml(String otp) {
        return """
                <div style="font-family:Arial,sans-serif;line-height:1.6;color:#0f172a">
                    <h2 style="color:#2563eb;margin:0 0 12px">Dề Dê Tickets</h2>
                    <p>Mã OTP đăng ký tài khoản của bạn là:</p>
                    <div style="font-size:28px;font-weight:700;letter-spacing:6px;color:#111827;margin:16px 0">%s</div>
                    <p>Mã có hiệu lực trong vài phút. Không chia sẻ mã này cho người khác.</p>
                </div>
                """.formatted(otp);
    }
}
