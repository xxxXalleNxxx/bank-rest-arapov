package com.example.bankcards.util;

import lombok.experimental.UtilityClass;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@UtilityClass
public class CardEncryptor {

    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "arapov-key-aes16";

    public static String encrypt(String cardNumber) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encryptedCard = cipher.doFinal(cardNumber.getBytes());

            return Base64.getEncoder().encodeToString(encryptedCard);

        } catch (Exception e) {
            throw new RuntimeException("Encrypt operation is failed", e);
        }
    }

    public static String decrypt(String encryptedCard) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decoded = Base64.getDecoder().decode(encryptedCard);

            byte[] decryptedCard = cipher.doFinal(decoded);

            return new String(decryptedCard);

        } catch (Exception e) {
            throw new RuntimeException("Decrypt operation is failed", e);
        }
    }
}
