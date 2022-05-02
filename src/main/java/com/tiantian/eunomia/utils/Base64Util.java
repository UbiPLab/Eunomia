package com.tiantian.eunomia.utils;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import lombok.var;

import java.math.BigInteger;
import java.util.Base64;

/**
 * @author tiantian152
 */
public class Base64Util {

    public static Pairing pairing = PairingFactory.getPairing("a1.properties");

    public static String elementToBase64(Element e) {
        byte[] bytes = e.toBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static Element base64ToElement(String base64Str) {
        byte[] byteArray = Base64.getDecoder().decode(base64Str);
        Element e = pairing.getG1().newElement();
        e.setFromBytes(byteArray);
        return e;
    }

    public static Element base64ToElementForZr(String base64Str) {
        byte[] byteArray = Base64.getDecoder().decode(base64Str);
        Element e = pairing.getZr().newElement();
        e.setFromBytes(byteArray);
        return e;
    }

    public static Element base64ToElementForGT(String base64Str) {
        byte[] byteArray = Base64.getDecoder().decode(base64Str);
        Element e = pairing.getGT().newElement();
        e.setFromBytes(byteArray);
        return e;
    }

    public static String bigIntegerToBase64(BigInteger bigInteger) {
        byte[] bytes = bigInteger.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static BigInteger base64ToBigInteger(String base64Str) {
        byte[] byteArray = Base64.getDecoder().decode(base64Str);
        return new BigInteger(byteArray);
    }

    public static String[] elementArrayToBase64Array(Element[] elements) {
        String[] base64s = new String[elements.length];
        for (int i = 0; i < elements.length; i++) {
            byte[] bytes = elements[i].toBytes();
            base64s[i] = Base64.getEncoder().encodeToString(bytes);
            System.out.println(base64s[i]);
        }
        return base64s;
    }

    public static String[] bigIntegerArrayToBase64Array(BigInteger[] bigIntegers) {
        String[] base64s = new String[bigIntegers.length];
        for (int i = 0; i < bigIntegers.length; i++) {
            byte[] bytes = bigIntegers[i].toByteArray();
            base64s[i] = Base64.getEncoder().encodeToString(bytes);
        }
        return base64s;
    }

    public static Element[] base64ArrayToElementArray(String[] base64Array) {
        Element[] elements = new Element[base64Array.length];
        for (int i = 0; i < base64Array.length; i++) {
            elements[i] = base64ToElement(base64Array[i]);
        }
        return elements;
    }

}
