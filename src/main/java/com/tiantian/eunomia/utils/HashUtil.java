package com.tiantian.eunomia.utils;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author tiantian152
 */
public class HashUtil {

    public static Pairing pairing = PairingFactory.getPairing("a1.properties");    //	生成pairing对象;

    public static Element h1(String s1) throws NoSuchAlgorithmException {    //	实现H1函数，完成01序列/字符串到G1的映射
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] mHash = md.digest(s1.getBytes());
        return pairing.getG1().newElementFromHash(mHash, 0, mHash.length);
    }

    public static Element h1Zr(String s) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] mHash = md.digest(s.getBytes());
        return pairing.getZr().newElementFromHash(mHash, 0, mHash.length);
    }

    /**
     * 实现H2函数，截取固定长度(l2=256)的01序列
     */
    public static String h2(String s2) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            // 调用digest方法，进行加密操作
            byte[] cipherBytes = messageDigest.digest(s2.getBytes());
            String h2 = "";
            for (byte i : cipherBytes) {
                h2 = h2 + Integer.toBinaryString((i & 0xFF) + 0x100).substring(1);
            }
            return h2;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String h3(Element g) {
        byte[] arr = g.toBytes();
        String s3 = "";
        for (byte i : arr) {
            s3 = s3 + Integer.toBinaryString((i & 0xFF) + 0x100).substring(1);
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            // 调用digest方法，进行加密操作
            byte[] cipherBytes = messageDigest.digest(s3.getBytes());
            String h3 = "";
            for (byte i : cipherBytes) {
                h3 = h3 + Integer.toBinaryString((i & 0xFF) + 0x100).substring(1);
            }
            return h3;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String h3(String s) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            // 调用digest方法，进行加密操作
            byte[] cipherBytes = messageDigest.digest(s.getBytes());
            String h3 = "";
            for (byte i : cipherBytes) {
                h3 = h3 + Integer.toBinaryString((i & 0xFF) + 0x100).substring(1);
            }
            return h3;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
