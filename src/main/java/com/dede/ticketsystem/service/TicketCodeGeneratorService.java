package com.dede.ticketsystem.service;

import com.dede.ticketsystem.repository.VeRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class TicketCodeGeneratorService {

    public static final String CODE_EXAMPLE = "DDT-7KQ9-HM4P-2XTA-R8VN";

    private static final String PREFIX = "DDT";
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int GROUP_COUNT = 4;
    private static final int GROUP_LENGTH = 4;
    private static final int MAX_RETRIES = 20;
    private static final Pattern CODE_PATTERN =
            Pattern.compile("^DDT-[A-HJ-NP-Z2-9]{4}(-[A-HJ-NP-Z2-9]{4}){3}$");
    private static final Pattern COMPACT_CODE_PATTERN =
            Pattern.compile("^DDT[A-HJ-NP-Z2-9]{16}$");

    private final SecureRandom secureRandom = new SecureRandom();
    private final VeRepository veRepository;

    public TicketCodeGeneratorService(VeRepository veRepository) {
        this.veRepository = veRepository;
    }

    public String generateUniqueTicketCode() {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String candidate = generateTicketCode();
            if (!veRepository.existsByMaQR(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Không thể sinh mã an toàn QR duy nhất sau " + MAX_RETRIES + " lần thử.");
    }

    public String normalizeCode(String rawCode) {
        if (rawCode == null) {
            return null;
        }
        String clean = rawCode.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
        if (clean.isEmpty()) {
            return null;
        }
        if (COMPACT_CODE_PATTERN.matcher(clean).matches()) {
            return PREFIX + "-" + clean.substring(3, 7)
                    + "-" + clean.substring(7, 11)
                    + "-" + clean.substring(11, 15)
                    + "-" + clean.substring(15, 19);
        }
        return clean;
    }

    public boolean isValidTicketCodeFormat(String rawCode) {
        String code = normalizeCode(rawCode);
        return code != null && CODE_PATTERN.matcher(code).matches();
    }

    public String normalizeAndValidateManualCode(String rawCode) {
        String code = normalizeCode(rawCode);
        if (!isValidTicketCodeFormat(code)) {
            throw new RuntimeException("Mã an toàn QR không đúng định dạng. Ví dụ: " + CODE_EXAMPLE);
        }
        return code;
    }

    public boolean isUnsafeLegacyCode(String maQR, String maVe) {
        String code = normalizeCode(maQR);
        if (code == null) {
            return true;
        }
        if (!isValidTicketCodeFormat(code)) {
            return true;
        }
        String upperCode = code.toUpperCase(Locale.ROOT);
        if (upperCode.startsWith("QR-") || upperCode.startsWith("QR_") || upperCode.startsWith("DEMO-QR-")) {
            return true;
        }
        String ticketId = normalizeCode(maVe);
        return ticketId != null && upperCode.contains(ticketId);
    }

    private String generateTicketCode() {
        StringBuilder builder = new StringBuilder(PREFIX);
        for (int group = 0; group < GROUP_COUNT; group++) {
            builder.append('-');
            for (int index = 0; index < GROUP_LENGTH; index++) {
                builder.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
            }
        }
        return builder.toString();
    }
}
