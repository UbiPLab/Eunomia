package com.tiantian.eunomia;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.model.MasterKey.MasterKey;
import com.tiantian.eunomia.model.MasterKey.MasterKeyBase64;
import com.tiantian.eunomia.model.PkMsk.PkMsk;
import com.tiantian.eunomia.service.DataUserEntityRegistration;
import com.tiantian.eunomia.service.impl.DataUserEntityRegistrationImpl;
import com.tiantian.eunomia.utils.Base64Util;
import com.tiantian.eunomia.utils.FileUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class jpbcTest {

    @Test
    public void Test01() {
        Pairing pairing = PairingFactory.getPairing("a1.properties");
        Element e = pairing.getZr().newRandomElement();
        System.out.println(e);
    }

    @Test
    public void Test02() {
        //Original byte[]
        byte[] bytes = "hello world".getBytes();

        //Base64 Encoded
        String encoded = Base64.getEncoder().encodeToString(bytes);
        System.out.println(encoded);

        //Base64 Decoded
        byte[] decoded = Base64.getDecoder().decode(encoded);

        //Verify original content
        System.out.println(new String(decoded));
    }

    @Test
    public void test03() {

        DataUserEntityRegistration dataUserEntityRegistration = new DataUserEntityRegistrationImpl();
        PkMsk pkMsk = dataUserEntityRegistration.generatePkMsk();

//        String g2 = Base64Util.elementToBase64(pkMsk.getPk().getG2());
//        String A1 = Base64Util.elementToBase64(pkMsk.getPk().getA1());
//        String A2 = Base64Util.elementToBase64(pkMsk.getPk().getA2());
//        String B1 = Base64Util.elementToBase64(pkMsk.getPk().getB1());
//        String B2 = Base64Util.elementToBase64(pkMsk.getPk().getB2());
//        Pk_base64 pk_base64 = new Pk_base64(g2,A1,A2,B1,B2);
//
//        String msk_g1 = Base64Util.elementToBase64(pkMsk.getMsk().getG1());
//        String msk_g2 = Base64Util.elementToBase64(pkMsk.getMsk().getG2());
//        String msk_a1 = Base64Util.elementToBase64(pkMsk.getMsk().getA1());
//        String msk_a2 = Base64Util.elementToBase64(pkMsk.getMsk().getA2());
//        String msk_c1 = Base64Util.elementToBase64(pkMsk.getMsk().getC1());
//        String msk_c2 = Base64Util.elementToBase64(pkMsk.getMsk().getC2());
//        String msk_d1 = Base64Util.elementToBase64(pkMsk.getMsk().getD1());
//        String msk_d2 = Base64Util.elementToBase64(pkMsk.getMsk().getD2());
//        String msk_d3 = Base64Util.elementToBase64(pkMsk.getMsk().getD3());
//        Msk_base64 msk_base64 = new Msk_base64(msk_g1,msk_g2,msk_a1,msk_a2,msk_c1,msk_c2,msk_d1,msk_d2,msk_d3);
//
//        PkMsk_base64 pkMsk_base64 = new PkMsk_base64(msk_base64, pk_base64);
//        System.out.println("pkMsk_base64="+pkMsk_base64);
//
//        // 保存在文件中
//        String username = "tiantian";
//        String fileName = username + "pkMsk_base64_json_byteArray";
//        System.out.println("pkMsk_base64="+pkMsk_base64);
//        String pkMsk_base64_json = JSONObject.toJSONString(pkMsk_base64);
//        System.out.println("pkMsk_json="+pkMsk_base64_json);
//        byte[] pkMsk_base64_json_byteArray = pkMsk_base64_json.getBytes(StandardCharsets.UTF_8);
//        System.out.println("pkMsk_json_byteArray="+ Arrays.toString(pkMsk_base64_json_byteArray));
//        System.out.println(filePath);
//        boolean result = FileUtil.byteArrayToFile(pkMsk_base64_json_byteArray, filePath, fileName);
//        System.out.println(result);
//
//        // 读取文件
//        String pkMsk_base64_json_read = FileUtil.ReadFile(filePath, fileName);
//        System.out.println("pkMsk_base64_json_read="+pkMsk_base64_json_read);
//        JSONObject pkMsk_base64_json_object_read = JSONObject.parseObject(pkMsk_base64_json_read);
//        System.out.println("pkMsk_base64_json_object_read="+pkMsk_base64_json_object_read);
//        PkMsk_base64 pkMsk_base64_read =  JSONObject.toJavaObject(pkMsk_base64_json_object_read, PkMsk_base64.class);
//        System.out.println("pkMsk_base64_read="+pkMsk_base64_read);

        String[] attr = new String[3];
        attr[0] = "policeNumber";
        attr[1] = "policeType";
        attr[2] = "policeStation";

        MasterKey sKey = dataUserEntityRegistration.generateSKey(attr, pkMsk.getMsk());
        System.out.println("sKey=" + sKey);

        // 转化为base64
        String[] sk1 = Base64Util.elementArrayToBase64Array(sKey.getSk1());
        Map<String, String[]> skx = new HashMap<>();
        for (Map.Entry<String, Element[]> entry : sKey.getSkx().entrySet()) {
            skx.put(entry.getKey(), Base64Util.elementArrayToBase64Array(entry.getValue()));
        }
        String[] sk3 = Base64Util.elementArrayToBase64Array(sKey.getSk3());

        MasterKeyBase64 sKey_base64 = new MasterKeyBase64(sk1, skx, sk3);
        System.out.println("sKey_base64=" + sKey_base64);

        // 转化成 Json
        String sKey_base64_json = JSONObject.toJSONString(sKey_base64);
        System.out.println("sKey_base64_json=" + sKey_base64_json);
        // 转化成 byte[]
        byte[] sKey_base64_json_byteArray = sKey_base64_json.getBytes(StandardCharsets.UTF_8);
        System.out.println("sKey_base64_json_byteArray=" + Arrays.toString(sKey_base64_json_byteArray));

        // 保存文件
        String username = "tiantian";
        String filePath = "./file";
        String fileName = username + "sKey_base64_json";
        boolean result = FileUtil.byteArrayToFile(sKey_base64_json_byteArray, filePath, fileName);
        System.out.println(result);

        // 读取文件
        // 读取String
        String sKey_base64_json_read = FileUtil.readFile(filePath, fileName);
        System.out.println("sKey_base64_json_read=" + sKey_base64_json_read);
        // 转化成Json对象
        JSONObject sKey_base64_json_object_read = JSONObject.parseObject(sKey_base64_json_read);
        System.out.println("sKey_base64_json_object_read=" + sKey_base64_json_object_read);
        // 转成 Java sKey_base64 对象
        MasterKeyBase64 sKey_base64_read = JSONObject.toJavaObject(sKey_base64_json_object_read, MasterKeyBase64.class);
        System.out.println("sKey_base64_read=" + sKey_base64_read);
        // 转成 Java sKey 对象
        Element[] sk1_element = Base64Util.base64ArrayToElementArray(sKey_base64_read.getSk1());
        Map<String, Element[]> skx_element = new HashMap<>();
        for (Map.Entry<String, String[]> entry : sKey_base64_read.getSkx().entrySet()) {
            skx_element.put(entry.getKey(), Base64Util.base64ArrayToElementArray(entry.getValue()));
        }
        Element[] sk3_element = Base64Util.base64ArrayToElementArray(sKey_base64_read.getSk3());
        MasterKey sKey_read = new MasterKey(sk1_element, skx_element, sk3_element);
        System.out.println("sKey_read=" + sKey_read);
    }

}
