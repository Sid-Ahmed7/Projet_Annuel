package com.glotrush.security.totp;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.glotrush.exceptions.DecryptException;
import com.glotrush.exceptions.EncryptException;
import com.glotrush.exceptions.QrCodeGenerationException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TotpService {

    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final DefaultCodeVerifier verifier;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int GCM_IV_LENGTH = 12; 
    private static final int GCM_TAG_LENGTH = 128;

    @Value("${totp.encryption}")
    private String encryptionKey; 

    public TotpService() {
        this.verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    }

    public String generateSecret() {
        return new DefaultSecretGenerator().generate();
    }

    public String generateQrCodeImageUri(String secret, String email, String issuer) {
        try {
            QrData data = new QrData.Builder()
                    .label(email)
                    .secret(secret)
                    .issuer(issuer)
                    .algorithm(HashingAlgorithm.SHA1)
                    .digits(6)
                    .period(30)
                    .build();

            String otpAuthUrl = data.getUri();
            
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUrl, BarcodeFormat.QR_CODE, 250, 250);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();
            
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(qrCodeBytes);
            
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            throw new QrCodeGenerationException("Failed to generate QR code", e);
        }
    }


    public boolean verifyCode(String secret, String code) {
        try {
            return verifier.isValidCode(secret, code);
        } catch (Exception e) {
            log.error("Error verifying TOTP code", e);
            return false;
        }
    }

   
    public String encryptSecret(String secret) {
        try {
            if (encryptionKey.length() != 32) {
                throw new IllegalStateException("Encryption key must be exactly 32 characters (256 bits)");
            }

            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
        
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
            byte[] encrypted = cipher.doFinal(secret.getBytes());
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
            byteBuffer.put(iv);
            byteBuffer.put(encrypted);
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Error encrypting secret", e);
            throw new EncryptException("Failed to encrypt secret", e);
        }
    }

public String decryptSecret(String encryptedSecret) {
    try {
        if (encryptionKey.length() != 32) {
            throw new IllegalStateException("Encryption key must be exactly 32 characters (256 bits)");
        }
        
        byte[] decodedData = Base64.getDecoder().decode(encryptedSecret);
        
        ByteBuffer byteBuffer = ByteBuffer.wrap(decodedData);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);
        byte[] encryptedData = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedData);
        SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
        
        byte[] decrypted = cipher.doFinal(encryptedData);
        return new String(decrypted);
        
    } catch (Exception e) {
        log.error("Error decrypting secret", e);
        throw new DecryptException("Failed to decrypt secret", e);
    }
}
}