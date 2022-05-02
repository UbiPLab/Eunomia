package com.tiantian.eunomia.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author tiantian152
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EcdsaUtils {

    private ECPublicKey ecPublicKey;
    private ECPrivateKey ecPrivateKey;
    private KeyPair keyPair;

    public void init() {
        try {
            keyPair = generateKey();
            ecPublicKey = (ECPublicKey) keyPair.getPublic();
            ecPrivateKey = (ECPrivateKey) keyPair.getPrivate();
            savePrivateToFile("./file", "ecPrivateKey");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public KeyPair generateKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(256);
        return keyPairGenerator.generateKeyPair();
    }

    public byte[] jdkEcdsa1(byte[] ecPrivateKey, String src) throws Exception {
        //	2.执行签名
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(ecPrivateKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Signature signature = Signature.getInstance("SHA1withECDSA");
        signature.initSign(privateKey);
        signature.update(src.getBytes());
        return signature.sign();
    }

    public boolean ecdsaVerify(byte[] ecPublicKey, String src, byte[] result) {
        //	3.验证签名
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(ecPublicKey);
        KeyFactory keyFactory;
        boolean bool = false;
        try {
            keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            Signature signature = Signature.getInstance("SHA1withECDSA");
            signature.initVerify(publicKey);
            signature.update(src.getBytes());
            bool = signature.verify(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bool;
    }

    public boolean savePrivateToFile(String filePath, String fileName) {
        return FileUtil.byteArrayToFile(ecPrivateKey.getEncoded(), filePath, fileName);
    }

}
