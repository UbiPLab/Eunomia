package com.tiantian.eunomia.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.mapper.HMapper;
import com.tiantian.eunomia.mapper.SystemInitMapper;
import com.tiantian.eunomia.model.Aci;
import com.tiantian.eunomia.model.MasterKey.MasterKey;

import com.tiantian.eunomia.model.TxEcdsa;
import com.tiantian.eunomia.model.msk.Msk;
import com.tiantian.eunomia.service.DataProviderEntityRegistration;
import com.tiantian.eunomia.utils.Base64Util;

import com.tiantian.eunomia.utils.EcdsaUtils;
import com.tiantian.eunomia.utils.FileUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author shubham
 */
@Service
@Component
public class DataProviderEntityRegistrationImpl implements DataProviderEntityRegistration {

    private static final Pairing PAIRING = PairingFactory.getPairing("a1.properties");

    @Value("${file.path}")
    String filePath;

    public static BigInteger ss = BigInteger.probablePrime(128, new Random());

    public static Element pii,ski,cri,ri,rii,h1;

    public static Msk msk;
    public static Element g1,g2,a1,a2,b1,b2,A1,A2,B1,B2,c1,c2,d1,d2,d3;

    public static EcdsaUtils ecdsa = new EcdsaUtils();

    public static Element r1 = PAIRING.getZr().newRandomElement().getImmutable();
    public static Element r2 = PAIRING.getZr().newRandomElement().getImmutable();
    public static Element[] skj1 = new Element[3];

    public static MasterKey skey = new MasterKey();

    @Autowired
    private HMapper hMapper;

    @Autowired
    private SystemInitMapper systemInitMapper;

    @Override
    public JSONObject generateTX1Json(String au, Aci aci) {
        Element cri = aci.getCri();
        String[] attr = aci.getAttr();
        Element pii = aci.getPii();
        Element auxi = aci.getAuxi();

        Date date = new Date();
        long ts = System.currentTimeMillis();

        Map<String, Object> map = new HashMap<>();
        map.put("operating", "Register");
        map.put("Authority", au);
        map.put("cri", Base64Util.elementToBase64(cri));
        for(int i = 0;i<attr.length;i++) {
            map.put("attr" + (i+1), attr[i]);
        }
        map.put("pii", Base64Util.elementToBase64(pii));
        map.put("auxi", Base64Util.elementToBase64(auxi));
        map.put("date", date.toString());
        map.put("ts", Long.toString(ts));

        return new JSONObject(map);
    }

    @Override
    public TxEcdsa generateTx1(String au, Aci aci) {
        JSONObject tx1Json = generateTX1Json(au, aci);
        String sInput = tx1Json.toJSONString();
        System.out.println("签名使用的json"+sInput);

        TxEcdsa tx1Ecdsa = null;
        try {
            ecdsa.init();
            byte[] publicKeyByteArray = ecdsa.getEcPublicKey().getEncoded();
            String publicKeyBase64 = DatatypeConverter.printBase64Binary(publicKeyByteArray);
            System.out.println("签名公钥="+publicKeyBase64);
            byte [] txEcdsa = null;
            txEcdsa = ecdsa.jdkEcdsa1(ecdsa.getEcPrivateKey().getEncoded(), sInput);
            System.out.println("签名后的json_byte数租="+ Arrays.toString(txEcdsa));
            System.out.println("签名公钥_byte数租="+ Arrays.toString(publicKeyByteArray));
            String txEcdsaBase64 = DatatypeConverter.printBase64Binary(txEcdsa);
            tx1Ecdsa = new TxEcdsa(publicKeyBase64, txEcdsaBase64);
            ecdsa.ecdsaVerify(ecdsa.getEcPublicKey().getEncoded(), sInput, txEcdsa);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tx1Ecdsa;
    }

    @Override
    public boolean saveCri(String cri, String username) {
        String fileName = username + "Cri_base64_json_byteArray";
        String criBase64Json = JSONObject.toJSONString(cri);
        byte[] criBase64JsonByteArray = criBase64Json.getBytes(StandardCharsets.UTF_8);
        return FileUtil.byteArrayToFile(criBase64JsonByteArray, filePath, fileName);
    }

    @Override
    public String readCri(String username) {
        String fileName = username + "Cri_base64_json_byteArray";
        // 读取String
        String criBase64JsonRead = FileUtil.readFile(filePath, fileName);
        System.out.println("Cri_base64_json_read="+criBase64JsonRead);

        return criBase64JsonRead;
    }

    @Override
    public BigInteger calculateAi(Element[] criArray, BigInteger N) {
        BigInteger bigintCriArray[] = new BigInteger[criArray.length];
        for(int i = 0;i < criArray.length;i++) {
            bigintCriArray[i] = criArray[i].toBigInteger();
        }

        int len = criArray.length;
        BigInteger temp = bigintCriArray[0];
        for(int i = 1;i<len;i++) {
            temp = temp.multiply(bigintCriArray[i]);
        }
        BigInteger bigintAi = ss.modPow(temp,N);
        return bigintAi;
    }

    @Override
    public BigInteger calculateWi(Element[] criArray, Element crj, BigInteger N) {
        BigInteger bigintCriArray[] = new BigInteger[criArray.length];
        for(int i = 0;i < criArray.length;i++) {
            bigintCriArray[i] = criArray[i].toBigInteger();
        }
        int len = criArray.length;
        BigInteger temp = bigintCriArray[0];
        for(int j = 1;j<len;j++) {
            temp = temp.multiply(bigintCriArray[j]);
        }
        temp = temp.divide(crj.toBigInteger());
        BigInteger bigintWi = ss.modPow(temp,N);
        Element wi = PAIRING.getZr().newRandomElement();
        wi = wi.set(bigintWi);
        return bigintWi;
    }

    @Override
    public boolean verifyAi(BigInteger Ai, BigInteger wi, Element cri, BigInteger N) {
        BigInteger ai1Temp = wi.modPow(cri.toBigInteger(),N);
        if(ai1Temp.equals(Ai)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean saveSki(Element ski, String username) {
        String skiBase64 = Base64Util.elementToBase64(ski);
        String skiBase64Json = JSONObject.toJSONString(skiBase64);
        byte[] skiBase64JsonByteArray = skiBase64Json.getBytes(StandardCharsets.UTF_8);
        String fileName = username + "ski_base64_json_byteArray";
        return FileUtil.byteArrayToFile(skiBase64JsonByteArray, filePath, fileName);
    }

    @Override
    public Element readSki(String username) {
        String fileName = username + "ski_base64_json_byteArray";
        // 读取String
        String skiBase64JsonRead = FileUtil.readFile(filePath, fileName);
        System.out.println("Cri_base64_json_read="+skiBase64JsonRead);
        String skiBase64String = skiBase64JsonRead.replace("\"","").replace("\"","");
        Element ski = Base64Util.base64ToElementForZr(skiBase64String);
        return ski;
    }
}
