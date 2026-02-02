package com.inventrik.digitalestore.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class CryptoUtil {

    private static final String ALGORITHM = "EC";
    private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";
    private static final String PROVIDER = "BC";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public PublicKey importPublicKey(String publicKeyBase64) throws Exception {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, PROVIDER);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new Exception("Failed to import public key", e);
        }
    }

    public boolean verifySignature(String challenge, String signatureBase64, PublicKey publicKey) {
        try {
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            byte[] derSignature = convertP1363ToDER(signatureBytes);

            Signature verifier = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
            verifier.initVerify(publicKey);
            verifier.update(challenge.getBytes("UTF-8"));

            return verifier.verify(derSignature);
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] convertP1363ToDER(byte[] p1363Signature) throws IOException {
        if (p1363Signature.length != 64) {
            throw new IllegalArgumentException("Invalid P1363 signature length");
        }

        byte[] r = new byte[32];
        byte[] s = new byte[32];
        System.arraycopy(p1363Signature, 0, r, 0, 32);
        System.arraycopy(p1363Signature, 32, s, 0, 32);

        BigInteger rInt = new BigInteger(1, r);
        BigInteger sInt = new BigInteger(1, s);

        byte[] rBytes = encodeDERInteger(rInt);
        byte[] sBytes = encodeDERInteger(sInt);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0x30);
        baos.write(rBytes.length + sBytes.length);
        baos.write(rBytes);
        baos.write(sBytes);

        return baos.toByteArray();
    }

    private byte[] encodeDERInteger(BigInteger value) throws IOException {
        byte[] bytes = value.toByteArray();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0x02);
        baos.write(bytes.length);
        baos.write(bytes);
        return baos.toByteArray();
    }
}
