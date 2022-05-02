package com.tiantian.eunomia.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.model.TxEcdsa;
import com.tiantian.eunomia.service.DataReportService;
import com.tiantian.eunomia.utils.EcdsaUtils;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shubham
 */
public class DataReportServiceImpl implements DataReportService {
    public static EcdsaUtils ecdsa = new EcdsaUtils();

    @Override
    public JSONObject generateTx7Json(String H2Rj) {
        Date date = new Date();
        long ts = System.currentTimeMillis();

        Map<String,Object> map = new HashMap<>(5);
        map.put("operating", "Report");
        map.put("H2Rj",H2Rj);
        map.put("date", date.toString());
        map.put("ts", Long.toString(ts));

        return new JSONObject(map);
    }

    @Override
    public TxEcdsa generateTx7Ecdsa(JSONObject tx7Json) {
        String sInput = tx7Json.toJSONString();
        System.out.println("签名使用的Tx7 json" + sInput);

        TxEcdsa tx7Ecdsa = null;
        try {
            ecdsa.init();
            byte[] publicKeyByteArray = ecdsa.getEcPublicKey().getEncoded();
            String publicKeyBase64 = DatatypeConverter.printBase64Binary(publicKeyByteArray);
            System.out.println("签名公钥=" + publicKeyBase64);
            byte[] txEcdsa = ecdsa.jdkEcdsa1(ecdsa.getEcPrivateKey().getEncoded(), sInput);
            System.out.println("签名公钥_byte数租=" + Arrays.toString(publicKeyByteArray));
            System.out.println("签名后的json_byte数租=" + Arrays.toString(txEcdsa));
            String txEcdsaBase64 = DatatypeConverter.printBase64Binary(txEcdsa);
            tx7Ecdsa = new TxEcdsa(publicKeyBase64, txEcdsaBase64);
            ecdsa.ecdsaVerify(ecdsa.getEcPublicKey().getEncoded(), sInput, txEcdsa);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tx7Ecdsa;
    }

    @Override
    public JSONObject generateTx8Json(String X) {
        Date date = new Date();
        long ts = System.currentTimeMillis();

        Map<String,Object> map = new HashMap<>(5);
        map.put("operating", "Report");
        map.put("X",X);
        map.put("date", date.toString());
        map.put("ts", Long.toString(ts));

        return new JSONObject(map);
    }

    @Override
    public TxEcdsa generateTx8Ecdsa(JSONObject tx8Json) {
        String sInput = tx8Json.toJSONString();
        System.out.println("签名使用的Tx6 json" + sInput);

        TxEcdsa tx8Ecdsa = null;
        try {
            ecdsa.init();
            byte[] publicKeyByteArray = ecdsa.getEcPublicKey().getEncoded();
            String publicKeyBase64 = DatatypeConverter.printBase64Binary(publicKeyByteArray);
            System.out.println("签名公钥=" + publicKeyBase64);
            byte[] txEcdsa = ecdsa.jdkEcdsa1(ecdsa.getEcPrivateKey().getEncoded(), sInput);
            System.out.println("签名公钥_byte数租=" + Arrays.toString(publicKeyByteArray));
            System.out.println("签名后的json_byte数租=" + Arrays.toString(txEcdsa));
            String txEcdsaBase64 = DatatypeConverter.printBase64Binary(txEcdsa);
            tx8Ecdsa = new TxEcdsa(publicKeyBase64, txEcdsaBase64);
            ecdsa.ecdsaVerify(ecdsa.getEcPublicKey().getEncoded(), sInput, txEcdsa);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tx8Ecdsa;
    }
}
