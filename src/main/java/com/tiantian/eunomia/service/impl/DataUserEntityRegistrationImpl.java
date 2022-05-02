package com.tiantian.eunomia.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.mapper.HMapper;
import com.tiantian.eunomia.mapper.SystemInitMapper;
import com.tiantian.eunomia.model.*;
import com.tiantian.eunomia.model.MasterKey.MasterKey;
import com.tiantian.eunomia.model.MasterKey.MasterKeyBase64;
import com.tiantian.eunomia.model.PkMsk.PkMsk;
import com.tiantian.eunomia.model.PkMsk.PkMskBase64;
import com.tiantian.eunomia.model.msk.Msk;
import com.tiantian.eunomia.model.msk.MskBase64;
import com.tiantian.eunomia.model.pk.Pk;
import com.tiantian.eunomia.model.pk.PkBase64;
import com.tiantian.eunomia.service.DataUserEntityRegistration;
import com.tiantian.eunomia.utils.*;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author tiantian152
 */
@Service
@Component
public class DataUserEntityRegistrationImpl implements DataUserEntityRegistration {

    public static Pairing pairing = PairingFactory.getPairing("a1.properties");

    @Value("${file.path}")
    String filePath;

    public static Element ski, cri, ri, h0, h1;

    public static Element g1, g2, a1, a2, b1, b2, b3, A1, A2, B1, B2, c1, c2, d1, d2, d3;

    public static final int ATTR_LENGTH = 5;

    /**
     * 计算Tx2使用
     */
    public static EcdsaUtils ecdsa = new EcdsaUtils();

    public static Element r1 = pairing.getZr().newRandomElement().getImmutable();
    public static Element r2 = pairing.getZr().newRandomElement().getImmutable();

    /**
     * 数据用户属性秘钥skey
     */
    public static MasterKey skey = new MasterKey();

    @Autowired
    private HMapper hMapper;

    @Autowired
    private SystemInitMapper systemInitMapper;

    @Override
    public Element generateSki(String password) {
        String str = HashUtil.h3(password);
        assert str != null;
        byte[] mHash = str.getBytes();
        return pairing.getZr().newElementFromHash(mHash, 0, mHash.length);
    }

    @Override
    public Element generateRi() {
        return pairing.getZr().newRandomElement().getImmutable();
    }

    @Override
    public Element calculatePii(Element ri, Element ski) {
        Element[] hElements = selectHFromSystemInit();
        h0 = hElements[0].getImmutable();
        h1 = hElements[1].getImmutable();

        Element pii1 = h0.powZn(ri).getImmutable();
        Element pii2 = h1.powZn(ski).getImmutable();
        return pii1.mul(pii2).getImmutable();
    }

    @Override
    public Element calculateCri(Element rii, Element ski) {
        Element[] hElements = selectHFromSystemInit();
        h0 = hElements[0].getImmutable();
        h1 = hElements[1].getImmutable();
        Element cri = h0.powZn(rii).getImmutable();
        cri = cri.mul(h1.powZn(ski));
        return cri;
    }

    @Override
    public Element[] generateCriProof(Element cri, Element rii, Element ski, Element[] attr) {

        Element[] hElements = selectHFromSystemInit();
        h0 = hElements[0].getImmutable();
        h1 = hElements[1].getImmutable();

        Element att = pairing.getG1().newRandomElement();
        att = att.setToOne();
        for (int i = 0; i < attr.length; i++) {
            att = att.mul(hElements[i + 2].powZn(attr[i]));
        }

        //  生成零知识证明凭证
        Element a = pairing.getZr().newRandomElement();
        Element b = pairing.getZr().newRandomElement();
        Element t = h1.powZn(b).getImmutable();
        t = (h0.powZn(a)).mul(t);
        t = t.mul(att);

        //  生成哈希值c
        Element c = EunomiaUtil.generateC(h0, h1, cri, t);

        Element r1 = a.sub(c.mul(rii));
        Element r2 = b.sub(c.mul(ski));

        Element[] proof = new Element[5];
        proof[0] = cri;
        proof[1] = t;
        proof[2] = r1;
        proof[3] = r2;
        proof[4] = att;

        return proof;
    }

    @Override
    public boolean verifyCriProof(Element[] proof, Element ri) {
        //  生成哈希值c
        Element[] hElements = selectHFromSystemInit();
        h0 = hElements[0].getImmutable();
        h1 = hElements[1].getImmutable();
        Element cri = proof[0];
        Element t = proof[1];
        Element c = EunomiaUtil.generateC(h0, h1, cri, t);

        Element left = (h0.powZn(proof[2])).mul(h1.powZn(proof[3])).mul(cri.powZn(c)).mul(proof[4]);

        return left.isEqual(t);
    }

    @Override
    public Element[] generatePiiProof(Element pii, Element ri, Element ski) {
        // 生成零知识证明凭证
        Element a = pairing.getZr().newRandomElement();
        Element b = pairing.getZr().newRandomElement();
        Element[] hElements = selectHFromSystemInit();
        h0 = hElements[0].getImmutable();
        h1 = hElements[1].getImmutable();

        Element t = h1.powZn(b).getImmutable();
        t = (h0.powZn(a)).mul(t);

        // 生成哈希值c
        Element c = EunomiaUtil.generateC(h0, h1, pii, t);

        Element r1 = a.sub(c.mul(ri)).getImmutable();
        Element r2 = b.sub(c.mul(ski)).getImmutable();

        Element[] proof = new Element[4];
        proof[0] = pii;
        proof[1] = t;
        proof[2] = r1;
        proof[3] = r2;

        return proof;
    }

    @Override
    public boolean verifyPiiProof(Element[] proof) {
        // 生成哈希值c
        Element[] hElements = selectHFromSystemInit();
        h0 = hElements[0].getImmutable();
        h1 = hElements[1].getImmutable();
        Element pii = proof[0];
        Element t = proof[1];
        Element c = EunomiaUtil.generateC(h0, h1, pii, t);

        Element left = (h0.powZn(proof[2])).mul(h1.powZn(proof[3])).mul(pii.powZn(c));

        return left.isEqual(t);
    }

    @Override
    public Element[] generateSkj1(Msk msk) {
        Element[] skj1 = new Element[3];
        skj1[0] = msk.getG2().powZn(r1.mul(msk.getC1()));
        skj1[1] = msk.getG2().powZn(r1.mul(msk.getC2()));
        skj1[2] = msk.getG2().powZn(r1.add(r2));
        skey.setSk1(skj1);
        return skj1;
    }

    @Override
    public Element[] generateSkj(String attr, Msk msk) {
        Element[] skjA = new Element[3];
        Element rx = pairing.getZr().newRandomElement().getImmutable();

        Element h11;
        try {
            h11 = HashUtil.h1(attr + "1" + "1").powZn((msk.getC1().mul(r1)).div(msk.getA1()));
            Element h12 = HashUtil.h1(attr + "2" + "1").powZn((msk.getC2().mul(r2)).div(msk.getA1()));
            Element h13 = HashUtil.h1(attr + "3" + "1").powZn((r1.add(r2)).div(msk.getA1()));
            Element g = msk.getD1().powZn(rx.div(msk.getA1()));
            Element skjx1 = h11.mul(h12).mul(h13).mul(g);
            Element h21 = HashUtil.h1(attr + "1" + "2").powZn((msk.getC1().mul(r1)).div(msk.getA2()));
            Element h22 = HashUtil.h1(attr + "2" + "2").powZn((msk.getC2().mul(r2)).div(msk.getA2()));
            Element h23 = HashUtil.h1(attr + "3" + "2").powZn((r1.add(r2)).div(msk.getA2()));
            Element g2 = msk.getG1().powZn(rx.div(msk.getA2()));
            Element skjx2 = h21.mul(h22).mul(h23).mul(g2);
            Element skjx3 = msk.getG1().powZn(rx.negate());
            skjA[0] = skjx1;
            skjA[1] = skjx2;
            skjA[2] = skjx3;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return skjA;
    }

    @Override
    public void generateMasterSkjx(String[] attrs, Msk msk) {
        Map<String, Element[]> skx = new HashMap<>(attrs.length);
        for (String attr : attrs) {
            Element[] skjx = generateSkj(attr, msk);
            skx.put(attr, skjx);
        }
        skey.setSkx(skx);
    }

    @Override
    public Element[] generateSkj3(Msk msk) {
        Element[] skj3 = new Element[3];
        Element rr = pairing.getZr().newRandomElement().getImmutable();
        Element hh11;
        try {
            hh11 = HashUtil.h1("0111").powZn((msk.getC1().mul(r1)).div(msk.getA1()));
            Element hh12 = HashUtil.h1("0121").powZn((msk.getC2().mul(r2)).div(msk.getA1()));
            Element hh13 = HashUtil.h1("0131").powZn((r1.add(r2)).div(msk.getA1()));
            skj3[0] = msk.getD1().mul(hh11).mul(hh12).mul(hh13).mul(msk.getG1().powZn(rr.div(msk.getA1())));
            Element hh21 = HashUtil.h1("0112").powZn((msk.getC1().mul(r1)).div(msk.getA2()));
            Element hh22 = HashUtil.h1("0122").powZn((msk.getC2().mul(r2)).div(msk.getA2()));
            Element hh23 = HashUtil.h1("0132").powZn((r1.add(r2)).div(msk.getA2()));
            skj3[1] = msk.getD2().mul(hh21).mul(hh22).mul(hh23).mul(msk.getG1().powZn(rr.div(msk.getA2())));
            skj3[2] = msk.getD3().mul(msk.getG1().powZn(rr.negate()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        skey.setSk3(skj3);
        return skj3;
    }

    @Override
    public JSONObject generateTx2Json(String au, Aci aci) {
        Element cri = aci.getCri();
        String[] attr = aci.getAttr();
        Element pii = aci.getPii();
        Element auxi = aci.getAuxi();

        Date date = new Date();
        long ts = System.currentTimeMillis();

        Map<String, Object> map = new HashMap<>(ATTR_LENGTH);
        map.put("operating", "Grant");
        map.put("Authority", au);
        map.put("cri", Base64Util.elementToBase64(cri));
        for (int i = 0; i < attr.length; i++) {
            map.put("attr" + (i + 1), attr[i]);
        }
        map.put("pii", Base64Util.elementToBase64(pii));
        map.put("auxi", Base64Util.elementToBase64(auxi));
        map.put("date", date.toString());
        map.put("ts", Long.toString(ts));

        return new JSONObject(map);
    }

    @Override
    public TxEcdsa generateTx2Ecdsa(JSONObject tx2Json) {
        String sInput = tx2Json.toJSONString();
        System.out.println("签名使用的Tx2 json" + sInput);

        TxEcdsa tx2Ecdsa = null;
        try {
            ecdsa.init();
            byte[] publicKeyByteArray = ecdsa.getEcPublicKey().getEncoded();
            String publicKeyBase64 = DatatypeConverter.printBase64Binary(publicKeyByteArray);
            System.out.println("签名公钥=" + publicKeyBase64);
            byte[] txEcdsa = ecdsa.jdkEcdsa1(ecdsa.getEcPrivateKey().getEncoded(), sInput);
            System.out.println("签名公钥_byte数租=" + Arrays.toString(publicKeyByteArray));
            System.out.println("签名后的json_byte数租=" + Arrays.toString(txEcdsa));
            String txEcdsaBase64 = DatatypeConverter.printBase64Binary(txEcdsa);
            tx2Ecdsa = new TxEcdsa(publicKeyBase64, txEcdsaBase64);
            ecdsa.ecdsaVerify(ecdsa.getEcPublicKey().getEncoded(), sInput, txEcdsa);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tx2Ecdsa;
    }

    @Override
    public PkMsk generatePkMsk() {
        //	获取G1的生成元
        g1 = pairing.getG1().newRandomElement().getImmutable();
        //	获取G2的生成元
        g2 = pairing.getG2().newRandomElement().getImmutable();
        for (int i = 0; i < ATTR_LENGTH; i++) {
            //	获取属于Zp群的随机数
            Element arrayab = pairing.getZr().newRandomElement().getImmutable();
            switch (i) {
                case 0:
                    a1 = arrayab;
                    break;
                case 1:
                    a2 = arrayab;
                    break;
                case 2:
                    b1 = arrayab;
                    break;
                case 3:
                    b2 = arrayab;
                    break;
                case 4:
                    b3 = arrayab;
                    break;
                default:
                    break;
            }
        }
        //	以下是计算pk和master_key里面包含的内容
        A1 = g2.powZn(a1);
        A2 = g2.powZn(a2);

        Element temp1 = pairing.pairing(g1, g2).getImmutable();
        B1 = temp1.powZn((a1.mul(b1)).add(b3)).getImmutable();
        B2 = temp1.powZn((a2.mul(b2)).add(b3)).getImmutable();

        c1 = pairing.getZr().newRandomElement().getImmutable();
        c2 = pairing.getZr().newRandomElement().getImmutable();

        d1 = g1.powZn(b1);
        d2 = g1.powZn(b2);
        d3 = g1.powZn(b3);

        Pk pk = new Pk(g2, A1, A2, B1, B2);
        Msk msk = new Msk(g1, g2, a1, a2, c1, c2, d1, d2, d3);

        return new PkMsk(msk, pk);
    }

    @Override
    public boolean savePkMsk(PkMsk pkMsk, String username) {
        // 转化成Base64版本
        String g2 = Base64Util.elementToBase64(pkMsk.getPk().getG2());
        String A1 = Base64Util.elementToBase64(pkMsk.getPk().getA1());
        String A2 = Base64Util.elementToBase64(pkMsk.getPk().getA2());
        String B1 = Base64Util.elementToBase64(pkMsk.getPk().getB1());
        String B2 = Base64Util.elementToBase64(pkMsk.getPk().getB2());
        PkBase64 pkBase64 = new PkBase64(g2, A1, A2, B1, B2);

        String mskG1 = Base64Util.elementToBase64(pkMsk.getMsk().getG1());
        String mskG2 = Base64Util.elementToBase64(pkMsk.getMsk().getG2());
        String mskA1 = Base64Util.elementToBase64(pkMsk.getMsk().getA1());
        String mskA2 = Base64Util.elementToBase64(pkMsk.getMsk().getA2());
        String mskC1 = Base64Util.elementToBase64(pkMsk.getMsk().getC1());
        String mskC2 = Base64Util.elementToBase64(pkMsk.getMsk().getC2());
        String mskD1 = Base64Util.elementToBase64(pkMsk.getMsk().getD1());
        String mskD2 = Base64Util.elementToBase64(pkMsk.getMsk().getD2());
        String mskD3 = Base64Util.elementToBase64(pkMsk.getMsk().getD3());
        MskBase64 mskBase64 = new MskBase64(mskG1, mskG2, mskA1, mskA2, mskC1, mskC2, mskD1, mskD2, mskD3);

        PkMskBase64 pkMskBase64 = new PkMskBase64(mskBase64, pkBase64);
        System.out.println("pkMsk_base64=" + pkMskBase64);

        // 保存在文件中
        String fileName = username + "pkMsk_base64_json_byteArray";
        String pkMskBase64Json = JSONObject.toJSONString(pkMskBase64);
        byte[] pkMskBase64JsonByteArray = pkMskBase64Json.getBytes(StandardCharsets.UTF_8);
        return FileUtil.byteArrayToFile(pkMskBase64JsonByteArray, filePath, fileName);
    }

    @Override
    public PkMsk readPkMsk(String username) {
        String fileName = username + "pkMsk_base64_json_byteArray";
        // 读取String
        String pkMskBase64JsonRead = FileUtil.readFile(filePath, fileName);
        System.out.println("pkMsk_base64_json_read=" + pkMskBase64JsonRead);
        // 转化成Json对象
        JSONObject pkMskBase64JsonObjectRead = JSONObject.parseObject(pkMskBase64JsonRead);
        System.out.println("pkMsk_base64_json_object_read=" + pkMskBase64JsonObjectRead);
        // 转成 Java PkMsk_base64 对象
        PkMskBase64 pkMskBase64Read = JSONObject.toJavaObject(pkMskBase64JsonObjectRead, PkMskBase64.class);
        System.out.println("pkMsk_base64_read=" + pkMskBase64Read);
        // 转成 Java PkMsk 对象
        Element g2 = Base64Util.base64ToElement(pkMskBase64Read.getPkBase64().getG2());
        Element A1 = Base64Util.base64ToElement(pkMskBase64Read.getPkBase64().getA1());
        Element A2 = Base64Util.base64ToElement(pkMskBase64Read.getPkBase64().getA2());
        Element B1 = Base64Util.base64ToElementForGT(pkMskBase64Read.getPkBase64().getB1());
        Element B2 = Base64Util.base64ToElementForGT(pkMskBase64Read.getPkBase64().getB2());
        Pk pk = new Pk(g2, A1, A2, B1, B2);

        Element mskG1 = Base64Util.base64ToElement(pkMskBase64Read.getMskBase64().getG1());
        Element mskG2 = Base64Util.base64ToElement(pkMskBase64Read.getMskBase64().getG2());
        Element mskA1 = Base64Util.base64ToElement(pkMskBase64Read.getMskBase64().getA1());
        Element mskA2 = Base64Util.base64ToElement(pkMskBase64Read.getMskBase64().getA2());
        Element mskC1 = Base64Util.base64ToElement(pkMskBase64Read.getMskBase64().getC1());
        Element mskC2 = Base64Util.base64ToElement(pkMskBase64Read.getMskBase64().getC2());
        Element mskD1 = Base64Util.base64ToElement(pkMskBase64Read.getMskBase64().getD1());
        Element mskD2 = Base64Util.base64ToElement(pkMskBase64Read.getMskBase64().getD2());
        Element mskD3 = Base64Util.base64ToElement(pkMskBase64Read.getMskBase64().getD3());
        Msk msk = new Msk(mskG1, mskG2, mskA1, mskA2, mskC1, mskC2, mskD1, mskD2, mskD3);

        return new PkMsk(msk, pk);
    }

    @Override
    public MasterKey generateSKey(String[] attrs, Msk msk) {
        generateSkj1(msk);
        generateMasterSkjx(attrs, msk);
        generateSkj3(msk);
        return skey;
    }

    @Override
    public boolean saveSKey(MasterKey sKey, String username) {

        // 转化为base64
        String[] sk1 = Base64Util.elementArrayToBase64Array(sKey.getSk1());
        Map<String, String[]> skx = new HashMap<>();
        for (Map.Entry<String, Element[]> entry : sKey.getSkx().entrySet()) {
            skx.put(entry.getKey(), Base64Util.elementArrayToBase64Array(entry.getValue()));
        }
        String[] sk3 = Base64Util.elementArrayToBase64Array(sKey.getSk3());

        // 转化成 Base64 对象
        MasterKeyBase64 sKeyBase64 = new MasterKeyBase64(sk1, skx, sk3);
        System.out.println("sKey_base64=" + sKeyBase64);

        // 转化成 Json
        String sKeyBase64Json = JSONObject.toJSONString(sKeyBase64);
        System.out.println("sKey_base64_json=" + sKeyBase64Json);

        // 转化成 byte[]
        byte[] sKeyBase64JsonByteArray = sKeyBase64Json.getBytes(StandardCharsets.UTF_8);
        System.out.println("sKey_base64_json_byteArray=" + Arrays.toString(sKeyBase64JsonByteArray));

        // 保存文件
        String fileName = username + "_sKey_base64_json";
        return FileUtil.byteArrayToFile(sKeyBase64JsonByteArray, filePath, fileName);
    }

    @Override
    public MasterKey readSKey(String username) {
        String fileName = username + "_sKey_base64_json";
        // 读取String
        String sKeyBase64JsonRead = FileUtil.readFile(filePath, fileName);
        // 转化成Json对象
        JSONObject sKeyBase64JsonObjectRead = JSONObject.parseObject(sKeyBase64JsonRead);
        // 转成 Java sKey_base64 对象
        MasterKeyBase64 sKeyBase64Read = JSONObject.toJavaObject(sKeyBase64JsonObjectRead, MasterKeyBase64.class);
        // 转成 Java sKey 对象
        Element[] sk1Element = Base64Util.base64ArrayToElementArray(sKeyBase64Read.getSk1());
        Map<String, Element[]> skxElement = new HashMap<>();
        for (Map.Entry<String, String[]> entry : sKeyBase64Read.getSkx().entrySet()) {
            skxElement.put(entry.getKey(), Base64Util.base64ArrayToElementArray(entry.getValue()));
        }
        Element[] sk3Element = Base64Util.base64ArrayToElementArray(sKeyBase64Read.getSk3());
        // 构造对象
        return new MasterKey(sk1Element, skxElement, sk3Element);
    }

    @Override
    public Element[] selectHFromSystemInit() {
        List<SystemInit> systemInits = systemInitMapper.selectAll();
        SystemInit systemInit = systemInits.get(systemInits.size() - 1);
        int hId = systemInit.getHId();
        H h = hMapper.selectById(hId);
        int hLen = 5;
        Element[] hElements = new Element[hLen];
        hElements[0] = Base64Util.base64ToElement(h.getH1());
        hElements[1] = Base64Util.base64ToElement(h.getH2());
        hElements[2] = Base64Util.base64ToElement(h.getH3());
        hElements[3] = Base64Util.base64ToElement(h.getH4());
        hElements[4] = Base64Util.base64ToElement(h.getH5());

        return hElements;
    }
}