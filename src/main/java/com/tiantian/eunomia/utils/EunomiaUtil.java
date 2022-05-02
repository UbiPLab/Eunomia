package com.tiantian.eunomia.utils;

import com.tiantian.eunomia.service.DataUserEntityRegistration;
import com.tiantian.eunomia.service.impl.DataUserEntityRegistrationImpl;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author tiantian152
 */
public class EunomiaUtil {

    private static final Pairing PAIRING = PairingFactory.getPairing("a1.properties");

    /**
     * 生成哈希值c
     *
     * @param h0       h0
     * @param h1       h1
     * @param criOrPii cri/pii
     * @param t        t
     * @return c
     */
    public static Element generateC(Element h0, Element h1, Element criOrPii, Element t) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] h0Byte = h0.toBytes();
        byte[] h1Byte = h1.toBytes();
        byte[] criByte = criOrPii.toBytes();
        byte[] tByte = t.toBytes();
        try {
            outputStream.write(h0Byte);
            outputStream.write(h1Byte);
            outputStream.write(criByte);
            outputStream.write(tByte);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //  call hash
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        assert messageDigest != null;
        messageDigest.update(outputStream.toByteArray());
        byte[] hash = messageDigest.digest();

        return PAIRING.getZr().newElementFromHash(hash, 0, hash.length).getImmutable();
    }
}
