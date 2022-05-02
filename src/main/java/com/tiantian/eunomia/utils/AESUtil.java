package com.tiantian.eunomia.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * @author tiantian152
 */
public class AESUtil {

    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        // 192 and 256 bits may not be available
        keyGenerator.init(128, sr);
        SecretKey skey = keyGenerator.generateKey();
        return skey.getEncoded();
    }

    public static byte[] encrypt(byte[] seed, byte[] plaintext) {
        byte[] raw;
        byte[] encrypted = null;
        try {
            raw = getRawKey(seed);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            encrypted = cipher.doFinal(plaintext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;
    }

    public static byte[] decrypt(byte[] seed, byte[] ciphertext) {
        byte[] raw;
        byte[] decrypted = null;
        try {
            raw = getRawKey(seed);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            decrypted = cipher.doFinal(ciphertext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypted;
    }
}
