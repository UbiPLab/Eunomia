package com.tiantian.eunomia.utils;

import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

public class ECDSA_info {

    public ECPublicKey ecPublicKey;
    public ECPrivateKey ecPrivateKey;
    public KeyPair keyPair;
    static EcdsaUtils ecdsa = new EcdsaUtils();

    public void init() throws Exception {
        keyPair = ecdsa.generateKey();
        ecPublicKey = (ECPublicKey) keyPair.getPublic();
        ecPrivateKey = (ECPrivateKey) keyPair.getPrivate();
    }
}
