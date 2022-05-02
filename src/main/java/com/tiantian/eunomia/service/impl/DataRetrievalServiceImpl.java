package com.tiantian.eunomia.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.model.Ed.Ed;
import com.tiantian.eunomia.model.MasterKey.MasterKey;
import com.tiantian.eunomia.model.TxEcdsa;
import com.tiantian.eunomia.model.msk.Msk;
import com.tiantian.eunomia.service.DataRetrievalService;
import com.tiantian.eunomia.utils.*;
import com.tiantian.eunomia.watermark.Combinepic;
import com.tiantian.eunomia.watermark.FileEncAndDec;
import com.tiantian.eunomia.watermark.ImageWaterMarkMain;
import com.tiantian.eunomia.watermark.Splitpic;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * 数据检索
 *
 * @author tiantian152
 */
@Service
@Component
public class DataRetrievalServiceImpl implements DataRetrievalService {


    public static Pairing pairing = PairingFactory.getPairing("a1.properties");
    public Element r = pairing.getZr().newRandomElement().getImmutable();
    public static EcdsaUtils ecdsa = new EcdsaUtils();

    @Value("${file.path}")
    String filePath;

    @Override
    public Element getR() {
        return r;
    }

    @Override
    public Element generateG1PowR(Element r, Msk msk) {
        return msk.getG1().powZn(r);
    }

    @Override
    public int[] generatePrkBi(Element prk) {
        // 生成prk的位分解bi
        byte[] bytes = prk.toBytes();
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%8s", Integer.toBinaryString(aByte & 0xff)).replace(" ", "0"));
        }
        int[] resultB = new int[result.length()];
        for (int i = 0; i < resultB.length; i++) {
            resultB[i] = Integer.parseInt(result.substring(i, i + 1));
        }
        int[] b = new int[resultB.length];
        for (int i = 0; i < resultB.length; i++) {
            b[b.length - 1 - i] = resultB[i];
        }
        return b;
    }

    @Override
    public Element[] generateRandomRi(int len) {
        Element[] ri = new Element[len];
        for (int i = 0; i < len; i++) {
            ri[i] = pairing.getZr().newRandomElement().getImmutable();
        }
        return ri;
    }

    @Override
    public Element[][] generateCommitment(int[] b, Element[] arrRi, Element gr, Msk msk) {
        // tao是秘钥的位数
        int tao = b.length;
        // 计算commitment Ci 和 Ri
        Element[] c = new Element[tao];
        Element r = pairing.getZr().newRandomElement();
        r.setToZero();
        Element[][] ciR = new Element[2][tao];
        for (int i = 0; i < tao; i++) {
            Element ri = arrRi[i];
            c[i] = gr.pow(BigInteger.valueOf(b[i]));
            c[i] = msk.getG1().powZn(ri).mul(c[i]).getImmutable();
            BigInteger n = new BigInteger("2");
            n = n.pow(i);
            Element e = ri.mul(n);
            ciR[1][i] = e;
        }
        ciR[0] = c;
        return ciR;
        // 1 2 3
        // R R R
    }

    @Override
    public Element generateC(Element[][] ciR) {
        Element[] ci = ciR[0];
        int tao = ci.length;
        Element c = pairing.getG1().newRandomElement();
        c = c.setToOne();
        for (int i = 0; i < tao; i++) {
            BigInteger n = new BigInteger("2");
            n = n.pow(i);
            c = c.mul(ci[i].pow(n));
        }
        return c;
    }

    @Override
    public boolean verifyC(Element C, Element R, Element puk, Msk msk) {
        Element right = msk.getG1().powZn(R).mul(puk.powZn(r));
        return C.isEqual(right);
    }

    @Override
    public String generateKij(int i, int j, Element Ci, Msk msk) {
        Element r = pairing.getZr().newRandomElement().getImmutable();
        Element k = (Ci.mul(msk.getG1().powZn(r).pow(BigInteger.valueOf(-j)))).powZn(r);
        return HashUtil.h3(k);
    }

    @Override
    public String generatePi(int i, Element Ci, Msk msk) {
        String pi1 = HashUtil.h2(generateKij(i, 0, Ci, msk));
        assert pi1 != null;
        String pi11 = HashUtil.h3(pi1);
        String pi2 = HashUtil.h2(generateKij(i, 1, Ci, msk));
        assert pi2 != null;
        String pi21 = HashUtil.h3(pi2);
        StringBuilder pi = new StringBuilder();
        for (int n = 0; i < Objects.requireNonNull(pi11).length(); n++) {
            assert pi21 != null;
            if (pi11.charAt(i) + pi21.charAt(i) == 1) {
                pi.append("1");
            } else {
                pi.append("0");
            }
        }
        return pi.toString();
    }

    @Override
    public Element[] generateCiProof(Element gr, Element ri, int bi, Msk msk) {
        //  计算Ci
        Element Ci = gr.pow(BigInteger.valueOf(bi));
        Ci = msk.getG1().powZn(ri).mul(Ci).getImmutable();
        // 生成零知识证明凭证
        Element a = pairing.getZr().newRandomElement();
        Element b = pairing.getZr().newRandomElement();
        Element t = gr.powZn(b);
        t = t.mul(msk.getG1().powZn(a));

        //  生成哈希值c
        Element c = EunomiaUtil.generateC(msk.getG1(), gr, Ci, t);

        Element r1 = a.sub(c.mul(ri));
        Element r2 = b.sub(c.mul(BigInteger.valueOf(bi)));

        Element[] proof = new Element[4];
        proof[0] = Ci;
        proof[1] = t;
        proof[2] = r1;
        proof[3] = r2;
        return proof;
    }

    @Override
    public boolean verifyCiProof(Element gr, Element[] proof, Msk msk) {
        //  生成哈希值c
        Element Ci = proof[0];
        Element t = proof[1];
        Element h0 = msk.getG1();
        Element h1 = gr;

        Element c = EunomiaUtil.generateC(h0, h1, Ci, t);

        Element left = (msk.getG1().powZn(proof[2])).mul(gr.powZn(proof[3])).mul(Ci.powZn(c));

        return left.isEqual(t);
    }

    @Override
    public String[][] generatePi(Element[] Ci, Element r, Msk msk) {
        Element gr = msk.getG1().powZn(r).getImmutable();
        int tao = Ci.length;
        String[][] ki = new String[tao][2];
        String[][] Pi = new String[2][tao];
        for (int i = 0; i < tao; i++) {
            Element ki0 = Ci[i].mul(gr.pow(new BigInteger("0")));
            ki0 = ki0.powZn(r);
            Element ki1 = Ci[i].div(gr);
            ki1 = ki1.powZn(r);
            ki[i][0] = HashUtil.h3(ki0);
            ki[i][1] = HashUtil.h3(ki1);
            String ki0Hash = HashUtil.h3(Objects.requireNonNull(HashUtil.h2(ki[i][0])));
            String ki1Hash = HashUtil.h3(Objects.requireNonNull(HashUtil.h2(ki[i][1])));
            assert ki0Hash != null;
            assert ki1Hash != null;
            Pi[0][i] = CalculateUtil.calculateXor(ki0Hash, ki1Hash);
            String kiHash = HashUtil.h2(ki[i][0]);
            assert kiHash != null;
            Pi[1][i] = HashUtil.h3(kiHash);
        }
        return Pi;
    }

    @Override
    public String[][] generateKi(Element[] Ci, Element r, Msk msk) {
        Element gr = msk.getG1().powZn(r).getImmutable();
        int tao = Ci.length;
        String[][] ki = new String[2][tao];


        for (int i = 0; i < tao; i++){
            Element ki0 = Ci[i].mul(gr.pow(new BigInteger("0")));
            ki0 = ki0.powZn(r);
            Element ki1 = Ci[i].div(gr);
            ki1 = ki1.powZn(r);
            ki[0][i] = Base64Util.elementToBase64(ki0);
            ki[1][i] = Base64Util.elementToBase64(ki1);
        }
        return ki;
    }

    @Override
    public String[] generatePpi(Element gr, Element[] Ri, String[] Pi, int[] b) {
        int tao = b.length;
        String[] k = new String[tao];
        String[] ppi = new String[tao];
        for (int i = 0; i < tao; i++) {
            k[i] = HashUtil.h3(gr.powZn(Ri[i]));
            if (b[i] == 0) {
                assert k[i] != null;
                ppi[i] = HashUtil.h3(Objects.requireNonNull(HashUtil.h2(k[i])));
            } else if (b[i] == 1) {
                assert k[i] != null;
                String kHash = HashUtil.h3(Objects.requireNonNull(HashUtil.h2(k[i])));
                assert kHash != null;
                ppi[i] = CalculateUtil.calculateXor(kHash, Pi[i]);
            }
        }
        return ppi;
    }

    @Override
    public String[] generateKi(Element gr, Element[] Ri, String[] Pi, int[] b) {
        int tao = b.length;
        String[] k = new String[tao];
        for (int i = 0; i < tao; i++){
            k[i] = HashUtil.h3(gr.powZn(Ri[i]));
        }
        return k;
    }

    @Override
    public boolean verifyPpi(String[] ppi, String[] ki0) {
        boolean a = true;
        for (int i = 0; i < ppi.length; i++) {
            a = a & ppi[i].equals(ki0[i]);
        }
        return a;
    }

    @Override
    public byte[] encrypt(Element di, String message) {
        // encrypted message:
        return AESUtil.encrypt(di.toBytes(), message.getBytes());
    }

    @Override
    public byte[] decrypt(MasterKey key, Ed edi, byte[] aesBuf) {
        for (String attr : edi.getCtiv().keySet()) {
            if (!key.getSkx().containsKey(attr)) {
                System.err.println("Policy not satisfied. (" + attr + ")");
                System.exit(2);
            }
        }

        // decrypt the intermediate AES key:
        Element prod1Gt = pairing.getGT().newOneElement().getImmutable();
        Element prod2Gt = pairing.getGT().newOneElement().getImmutable();
        for (int i = 0; i < 3; i++) {
            Element prodH = pairing.getG1().newOneElement();
            Element prodG = pairing.getG1().newOneElement();
            for (String node : edi.getCtiv().keySet()) {
                // will be useful if MSP is complete
                String attr = node;
                String attrStripped = node;
                prodH.mul(key.getSkx().get(attrStripped)[i]);
                prodG.mul(edi.getCtiv().get(attr)[i]);
            }
            Element kpProdH = key.getSk3()[i].getImmutable();
            kpProdH = kpProdH.mul(prodH);
            prod1Gt = prod1Gt.mul(pairing.pairing(kpProdH, edi.getCt0()[i]));
            prod2Gt = prod2Gt.mul(pairing.pairing(prodG, key.getSk1()[i]));
        }
        Element aesKey = edi.getCtti().getImmutable();
        aesKey = aesKey.mul(prod2Gt);
        aesKey = aesKey.div(prod1Gt);
        return AESUtil.decrypt(aesKey.toBytes(), aesBuf);
    }

    @Override
    public void retrieval(String[] attrs, String message, Msk msk) {
        System.out.println("================数据检索================");

        // 生成g1^r
        Element gr = generateG1PowR(r, msk);
        System.out.println("gr=" + gr);

        // 计算prk的位分解
        Element prk = pairing.getZr().newRandomElement().getImmutable();
        int[] prkBi = generatePrkBi(prk);
        System.out.println(prkBi.length);
        System.out.println("prkBi=" + Arrays.toString(prkBi));

        // 产生一组随机值ri
        Element[] ri = generateRandomRi(prkBi.length);
        System.out.println("ri=" + Arrays.toString(ri));

        // 计算 Ci 和 Ri
        Element puk = msk.getG1().powZn(prk).getImmutable();
        Element[][] ciR = generateCommitment(prkBi, ri, gr, msk);
        Element[] Ci = ciR[0];
        Element[] Ri = ciR[1];
        System.out.println("Ci=" + Arrays.toString(Ci));
        System.out.println("Ri=" + Arrays.toString(Ri));

        //  计算R
        Element R = pairing.getZr().newZeroElement().getImmutable();
        for (Element e : ciR[1]) {
            R = R.add(e);
        }
        System.out.println("R=" + R);

        // 计算C
        Element C = generateC(ciR);
        System.out.println("C=" + C);

        //  验证 C = g1^r * puk^r
        boolean resultA = verifyC(C, R, puk, msk);
        System.out.println("C验证结果=" + resultA);

        //  零知识证明
        boolean resultB = true;
        for (int i = 0; i < ri.length; i++) {
            Element[] proof = generateCiProof(gr, ri[i], prkBi[i], msk);
            resultB = resultB & verifyCiProof(gr, proof, msk);
        }
        System.out.println("proof验证结果=" + resultB);
        boolean result = resultA & resultB;
        if (result) {
            System.out.println("数据检索零知识证明成功");
        } else {
            System.out.println("零知识失败");
        }

        String[][] P = generatePi(Ci, r, msk);
        String[] Pi = P[0];
        String[] ki0 = P[1];
        String[] PPi = generatePpi(gr, ri, Pi, prkBi);
        boolean result2 = verifyPpi(PPi, ki0);
        System.out.println("不经意传输验证" + result2);

////		Data_Uploading1.gen_ct(attrs);
////		Entity_Registration2.generate_skj1();
////		Entity_Registration2.gen_master_skjx(attrs);
////		Entity_Registration2.generate_skj3();
//        byte[] aesBuf = encrypt(edi.di,message);
////		for(byte x:aesBuf) {
////			System.out.print(x+" ");
////		}
////		System.out.println();
//        //System.out.println(decrypt(key,edi,aesBuf,edi.di));
//        byte[] byte_message = decrypt(key,edi,aesBuf);
////		for(byte x:byte_message) {
////			System.out.print(x+" ");
////		}
////		System.out.println();
//        String mes = new String(byte_message);
//        System.out.print(mes);
    }

    @Override
    public JSONObject generateTx5Json(String au, Element[] pi) {

        Date date = new Date();
        long ts = System.currentTimeMillis();

        Map<String,Object> map = new HashMap<>();
        map.put("operating","Permit");
        map.put("Authority",au);
        for (int i = 0; i < pi.length; i++) {
            map.put("pij" + Integer.toString(i + 1), pi[i].toString());
        }
        map.put("data",date.toString());
        map.put("ts",Long.toString(ts));
        return new JSONObject(map);
    }

    @Override
    public TxEcdsa generateTx5Ecdsa(JSONObject tx5Json){
        String sInput = tx5Json.toJSONString();
        System.out.println("签名使用的Tx5 json" + sInput);

        TxEcdsa tx5Ecdsa = null;
        try {
            ecdsa.init();
            byte[] publicKeyByteArray = ecdsa.getEcPublicKey().getEncoded();
            String publicKeyBase64 = DatatypeConverter.printBase64Binary(publicKeyByteArray);
            System.out.println("签名公钥=" + publicKeyBase64);
            byte[] txEcdsa = ecdsa.jdkEcdsa1(ecdsa.getEcPrivateKey().getEncoded(), sInput);
            System.out.println("签名公钥_byte数租=" + Arrays.toString(publicKeyByteArray));
            System.out.println("签名后的json_byte数租=" + Arrays.toString(txEcdsa));
            String txEcdsaBase64 = DatatypeConverter.printBase64Binary(txEcdsa);
            tx5Ecdsa = new TxEcdsa(publicKeyBase64,txEcdsaBase64);
            ecdsa.ecdsaVerify(ecdsa.getEcPublicKey().getEncoded(), sInput, txEcdsa);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tx5Ecdsa;
    }

    @Override
    public JSONObject generateTx6Json(String cpd, Element[] pi) {
        Date date = new Date();
        long ts = System.currentTimeMillis();

        Map<String,Object> map = new HashMap<>();
        map.put("operating","Analyze");
        map.put("CPD",cpd);
        for (int i = 0; i < pi.length; i++) {
            map.put("pij" + Integer.toString(i + 1), pi[i].toString());
        }
        map.put("data",date.toString());
        map.put("ts",Long.toString(ts));
        return new JSONObject(map);
    }

    @Override
    public TxEcdsa generateTx6Ecdsa(JSONObject tx6Json) {
        String sInput = tx6Json.toJSONString();
        System.out.println("签名使用的Tx6 json" + sInput);

        TxEcdsa tx6Ecdsa = null;
        try {
            ecdsa.init();
            byte[] publicKeyByteArray = ecdsa.getEcPublicKey().getEncoded();
            String publicKeyBase64 = DatatypeConverter.printBase64Binary(publicKeyByteArray);
            System.out.println("签名公钥=" + publicKeyBase64);
            byte[] txEcdsa = ecdsa.jdkEcdsa1(ecdsa.getEcPrivateKey().getEncoded(), sInput);
            System.out.println("签名公钥_byte数租=" + Arrays.toString(publicKeyByteArray));
            System.out.println("签名后的json_byte数租=" + Arrays.toString(txEcdsa));
            String txEcdsaBase64 = DatatypeConverter.printBase64Binary(txEcdsa);
            tx6Ecdsa = new TxEcdsa(publicKeyBase64, txEcdsaBase64);
            ecdsa.ecdsaVerify(ecdsa.getEcPublicKey().getEncoded(), sInput, txEcdsa);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tx6Ecdsa;
    }

    @Override
    public String[][] encWaterMask(String path, String[] ki0, String[] ki1) {
        long startTime = System.currentTimeMillis();
        String[][] Path = new String[160][2];
        try {
            Splitpic.split_picture(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageWaterMarkMain.watermark_in();
        for(int i=0;i<160;i++) {
            String key0 = ki0[i];
            String key1 = ki1[i];
            String hex0 = new BigInteger(key0,2).toString(16);

            if(hex0.length()==63){
                hex0 = "0" + hex0;
            }else if(hex0.length()==62){
                hex0 = "00" + hex0;
            }
            String hex1 = new BigInteger(key1,2).toString(16);
            if(hex1.length()==63){
                hex1 = "0" + hex1;
            }else if(hex1.length()==62){
                hex1 = "00" + hex1;
            }
            byte[] byte_key0 = FileEncAndDec.HexStringToBinary(hex0);
            byte[] byte_key1 = FileEncAndDec.HexStringToBinary(hex1);
//            byte[] byte_key0 = new byte[32];
//            byte[] byte_key1 = new byte[32];
//
//            for (int j = 0; j < 32; j++) {
//                byte_key0[j] = (byte) key0.charAt(j);
//                byte_key1[j] = (byte) key1.charAt(j);
//
//            }

            System.out.println("byte_key0="+ Arrays.toString(byte_key0));
            System.out.println("byte_key1="+ Arrays.toString(byte_key1));
            String path0 = "D:\\picture\\split1\\img0 "+i+".bmp";
            String path1 = "D:\\picture\\split1\\img1 "+i+".bmp";
            String encpath0 = "D:\\picture\\enc\\img0 "+i+"enc.tif";
            String encpath1 = "D:\\picture\\enc\\img1 "+i+"enc.tif";
            System.out.println("path0="+encpath0);

            try {
                FileEncAndDec.EncFile(new File(path0), new File(encpath0),byte_key0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                FileEncAndDec.EncFile(new File(path1), new File(encpath1),byte_key1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Path[i][0] = encpath0;
            Path[i][1] = encpath1;

            System.out.println("Path="+Path[i][0]);
        }
        System.out.println("加密成功");
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间1：" + (endTime - startTime) + "ms");
        return Path;
    }

    @Override
    public String decWaterMask(String[][] path, String[] key) throws IOException {
        long startTime2 = System.currentTimeMillis();
        for(int i=0;i<160;i++) {
//            byte[] byte_key = new byte[32];
//            for (int j = 0; j < 32; j++) {
//                byte_key[j] = (byte) key[i].charAt(j);
//
//            }
            String hex = new BigInteger(key[i],2).toString(16);
            if(hex.length()==63){
                hex = "0" + hex;
            }else if(hex.length()==62){
                hex = "00" + hex;
            }
            byte[] byte_key = FileEncAndDec.HexStringToBinary(hex);
//            byte[] byte_key = key[i].getBytes();
            System.out.println(byte_key);
            String encpath0 = path[i][0];
            String encpath1 = path[i][1];
            String decpath0 = "D:\\picture\\dec\\img0 "+i+".bmp";
            String decpath1 = "D:\\picture\\dec\\img1 "+i+".bmp";
            try {
                FileEncAndDec.DecFile(new File(encpath0), new File(decpath0),byte_key);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                FileEncAndDec.DecFile(new File(encpath1), new File(decpath1),byte_key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("解密成功");

        String combinePath = Combinepic.combine_picture();

        long endTime2 = System.currentTimeMillis();
        System.out.println("程序运行时间2：" + (endTime2 - startTime2) + "ms");

        return combinePath;
    }

    @Override
    public boolean saveKi0(String[] Ki0, String evidenceId) {
        byte[] KiByte = FileUtil.serialize(Ki0);
        System.out.println("Ki0Byte");
        String fileName = evidenceId + "Ki0_String_Array";
        return FileUtil.byteArrayToFile(KiByte,filePath,fileName);
    }

    @Override
    public String[] readKi0(String evidenceId) {
        String fileName = evidenceId + "Ki0_String_Array";
        byte[] KiByte = FileUtil.getBytesByFile(filePath,fileName);
        String[] KiString = FileUtil.unserialize(KiByte);
        return KiString;
    }

    @Override
    public boolean saveKi1(String[] Ki1, String evidenceId) {
        byte[] KiByte = FileUtil.serialize(Ki1);
        System.out.println("Ki1Byte");
        String fileName = evidenceId + "Ki1_String_Array";
        return FileUtil.byteArrayToFile(KiByte,filePath,fileName);
    }

    @Override
    public String[] readKi1(String evidenceId) {
        String fileName = evidenceId + "Ki1_String_Array";
        byte[] KiByte = FileUtil.getBytesByFile(filePath,fileName);
        String[] KiString = FileUtil.unserialize(KiByte);
        return KiString;
    }
}
