package com.tiantian.eunomia;

import com.tiantian.eunomia.utils.Base64Util;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.junit.jupiter.api.Test;

public class ElementTo256HexTest {

    public static Pairing pairing = PairingFactory.getPairing("a1.properties");    //	生成pairing对象;

    @Test
    public void testBase64() {
        Base64Util base64Util = new Base64Util();
        Element element = pairing.getG1().newRandomElement();
        System.out.println(element);
        String temp = base64Util.elementToBase64(element);
        System.out.println(temp);
        Element element1 = base64Util.base64ToElement(temp);
        System.out.println(element1);
    }

    @Test
    public void test02() {
        Element rii = pairing.getZr().newRandomElement().getImmutable();
        System.out.println(rii);
        String base64 = Base64Util.elementToBase64(rii);
        System.out.println(base64);
        Element riii = Base64Util.base64ToElementForZr(base64);
        System.out.println(riii);
    }
}
