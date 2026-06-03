package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.Ve;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Locale;

@Service
public class QRCodeService {

    public String buildTicketPayload(Ve ve) {
        if (ve == null) {
            return "";
        }
        String code = ve.getMaQR() == null ? "" : ve.getMaQR().trim().toUpperCase(Locale.ROOT);
        if (code.isEmpty()) {
            return "";
        }
        return "DDE_TICKET|v=1|code=" + code;
    }

    public String generateQRCodeBase64(String payload, int width, int height) {
        if (payload == null || payload.trim().isEmpty()) {
            return "";
        }
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            return Base64.getEncoder().encodeToString(pngData);
        } catch (Exception e) {
            System.err.println("Lỗi khi sinh mã QR Code Base64: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
}
