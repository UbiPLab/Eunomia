package com.tiantian.eunomia.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.mapper.dataUpload.EdMapper;
import com.tiantian.eunomia.model.Ed.Ed;
import com.tiantian.eunomia.model.Ed.EdBase64;
import com.tiantian.eunomia.model.Ed.EdBase64Json;
import com.tiantian.eunomia.model.MSP;
import com.tiantian.eunomia.model.TxEcdsaByCri;
import com.tiantian.eunomia.model.pk.Pk;
import com.tiantian.eunomia.service.DataUploadService;
import com.tiantian.eunomia.utils.*;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author shubham
 */
@Service
@Component
public class DataUploadServiceImpl implements DataUploadService {

    @Autowired
    private EdMapper edMapper;

    public static Pairing pairing = DataUserEntityRegistrationImpl.pairing;

    public static Ed edi = new Ed();

    public static ECDSA_info ecdsa_info = new ECDSA_info();
    public static EcdsaUtils ecdsa = new EcdsaUtils();

    @Value("${file.path}")
    String filePath;

    @Override
    public void genCt(String[] attrs, Pk pk) {

        Element u1 = pairing.getZr().newRandomElement().getImmutable();
        Element u2 = pairing.getZr().newRandomElement().getImmutable();
        Element[] u = new Element[2];
        u[0] = u1;
        u[1] = u2;
        //	生成ct0
        Element[] ct0 = new Element[3];
        ct0[0] = pk.A1.powZn(u1);
        ct0[1] = pk.A2.powZn(u2);
        ct0[2] = pk.g2.powZn(u1.add(u2));

        //	MSP:
        Map<String, int[]> msp = MSP.convert_policy_to_msp(attrs);
        //	生成ctiv

//        Map<String, Element[]> ctiv = new HashMap<>();
//		for(String attr:attrs) {
//			int[] row = msp.get(attr);
//			//  生成cti
//			Element[] cti = new Element[3];
//			for(int v = 1;v < 4;v++) {
//				Element prod = pairing.getG1().newOneElement().getImmutable();
//				for(int o = 1;o < n2 + 1;o++) {
//					Element prod1 = System_Initialization.H1("0"+o+v+"1").powZn(u1);
//					Element prod2 = System_Initialization.H1("0"+o+v+"2").powZn(u2);
//					prod = prod.mul(prod1).mul(prod2);
//					Element rowo = pairing.getZr().newElement(row[o-1]);
//					prod = prod.powZn(rowo);
//				}
//				Element prod3 = System_Initialization.H1(attr+v+1).powZn(u1);
//				Element prod4 = System_Initialization.H1(attr+v+2).powZn(u2);
//				prod = prod.mul(prod3).mul(prod4);
//				cti[v-1] = prod;
//			}
//			ctiv.put(attr,cti);
//		}


        int numCols = msp.size();
        // pre-compute hashes
        ArrayList<ArrayList<ArrayList<Element>>> hashTable = new ArrayList<>();
        for (int j = 0; j < numCols; j++) {
            ArrayList<ArrayList<Element>> x = new ArrayList<>();
            String inputForHash1 = "0" + (j + 1);
            for (int l = 0; l < 3; l++) {
                ArrayList<Element> y = new ArrayList<>();
                int l2 = l + 1;
                String inputForHash2 = inputForHash1 + l2;
                for (int t = 0; t < 2; t++) {
                    int t2 = t + 1;
                    String inputForHash3 = inputForHash2 + t2;
                    //System.out.println("enc - input_for_hash3: "+input_for_hash3);
                    Element hashedValue = pairing.getG1().newElement();
                    try {
                        hashedValue = HashUtil.h1(inputForHash3);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    y.add(hashedValue);
                }
                x.add(y);
            }
            hashTable.add(x);
        }

        Map<String, Element[]> ctiv = new HashMap<>();
        for (Map.Entry<String, int[]> entry : msp.entrySet()) {
            String attr = entry.getKey();
            //System.out.println(attr);
            int[] row = entry.getValue();
            Element[] ct = new Element[3];
            String attrStripped = attr;
            for (int l = 0; l < ct.length; l++) {
                Element prod = pairing.getG1().newOneElement();
                int cols = row.length;
                for (int t = 0; t < 2; t++) {
                    int l2 = l + 1;
                    int t2 = t + 1;
                    String inputForHash = attrStripped + l2 + t2;
                    //System.out.println("enc - input_for_hash: "+input_for_hash);
                    Element prod1 = pairing.getG1().newElement();
                    try {
                        prod1 = HashUtil.h1(inputForHash);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    for (int j = 0; j < cols; j++) {
                        Element rowj = pairing.getZr().newElement(row[j]);
                        Element hashTableJlt = hashTable.get(j).get(l).get(t).duplicate();
                        hashTableJlt.powZn(rowj);
                        prod1.mul(hashTableJlt);
                    }
                    Element prodPowS = prod1.duplicate();
                    prodPowS.powZn(u[t]);
                    prod.mul(prodPowS);
                }
                ct[l] = prod;
            }
            ctiv.put(attr, ct);
        }


        //	生成ct'i
        Element di = pairing.getGT().newRandomElement().getImmutable();
        Element ctti = pk.B1.powZn(u1).mul(pk.B2.powZn(u2)).mul(di);

        edi.ct0 = ct0;
        edi.ctiv = ctiv;
        edi.ctti = ctti;
        edi.di = di;
    }

    @Override
    public Ed uploading(String[] attrs, Pk pk) {
        genCt(attrs, pk);
        return edi;
    }

    @Override
    public String transform(Ed edi) {

        //转化为base64
        String[] ct0 = Base64Util.elementArrayToBase64Array(edi.ct0);
        Map<String, String[]> ctiv = new HashMap<>();
        for (Map.Entry<String, Element[]> entry : edi.getCtiv().entrySet()) {
            ctiv.put(entry.getKey(), Base64Util.elementArrayToBase64Array(entry.getValue()));
        }
        String ctti = Base64Util.elementToBase64(edi.getCtti());
        String di = Base64Util.elementToBase64(edi.getDi());
        // 转化成 Base64 对象
        EdBase64 edBase64 = new EdBase64(ct0, ctiv, ctti, di);
        System.out.println("base64 = " + edBase64);

        // 转化成 Json
        String edBase64Json = JSONObject.toJSONString(edBase64);
        System.out.println("base64json = " + edBase64Json);

        return edBase64Json;
    }

    @Override
    public EdBase64 jsonTransform(String EdJson) {
        JSONObject EdBase64JsonObject = JSONObject.parseObject(EdJson);
        EdBase64 edBase64 = JSONObject.toJavaObject(EdBase64JsonObject,EdBase64.class);

        return edBase64;
    }

    @Override
    public EdBase64 transformBase64(Ed edi) {
        //转化为base64
        String[] ct0 = Base64Util.elementArrayToBase64Array(edi.ct0);
        Map<String, String[]> ctiv = new HashMap<>();
        for (Map.Entry<String, Element[]> entry : edi.getCtiv().entrySet()) {
            ctiv.put(entry.getKey(), Base64Util.elementArrayToBase64Array(entry.getValue()));
        }
        String ctti = Base64Util.elementToBase64(edi.getCtti());
        String di = Base64Util.elementToBase64(edi.getDi());
        // 转化成 Base64 对象
        EdBase64 edBase64 = new EdBase64(ct0, ctiv, ctti, di);
        System.out.println(edBase64);
        return edBase64;
    }

    @Override
    public Ed getEd(EdBase64 edBase64) {
        Element[] ct0 = Base64Util.base64ArrayToElementArray(edBase64.getCt0());
        Map<String,Element[]> ctiv = new HashMap<>();
        for (Map.Entry<String,String[]> entry : edBase64.getCtiv().entrySet()){
            ctiv.put(entry.getKey(),Base64Util.base64ArrayToElementArray(entry.getValue()));
        }
        Element ctti = Base64Util.base64ToElementForGT(edBase64.getCtti());
        Element di = Base64Util.base64ToElement(edBase64.getDi());
        //转化为 Ed 对象
        Ed edi = new Ed(ct0,ctiv,ctti,di);
        System.out.println("转化成Ed: " + edi);

        return edi;
    }

    @Override
    public boolean saveEd(EdBase64 edBase64,String tel) {
        String EdBase64Json = JSONObject.toJSONString(edBase64);
        System.out.println("Ed_basa64_json=" + EdBase64Json);

        //转化为 byte[]
        byte[] EdBase64JsonByteArray = EdBase64Json.getBytes(StandardCharsets.UTF_8);
        System.out.println("Ed_Base64_Json_ByteArray");

        //保存文件
        String fileName = tel +"Ed_base64_json";
        return FileUtil.byteArrayToFile(EdBase64JsonByteArray, filePath, fileName);
    }

    @Override
    public EdBase64 readEd(String tel) {
        String fileName = tel + "Ed_base64_json";
        //读取String
        String EdBase64JsonRead = FileUtil.readFile(filePath,fileName);
        //转化成Json对象
        JSONObject EdBase64JsonObject = JSONObject.parseObject(EdBase64JsonRead);
        // 转成 Java sKey_base64 对象
        EdBase64 edBase64 = JSONObject.toJavaObject(EdBase64JsonObject,EdBase64.class);

        return edBase64;
    }

    @Override
    public String calculateHv1(Element pii, Element[] pIi, Element[] Ci, String[] mdi) {
        //	pii--->零知识证明中的pii   pIi--->零知识证明的凭证(此处为上一阶段的凭证）   ci--->从以太坊区块链处获取的一系列凭证的集合
        //	mdi--->metadata如：(“out-vehicle”, “photo”, 20200523, 21 : 30)
        String s = pii.toString();
        for (Element e : pIi) {
            s = s + e.toString();
        }
        for (Element e : Ci) {
            s = s + e.toString();
        }
        for (String str : mdi) {
            s = s + str.toString();
        }
        String result = HashUtil.h2(s);
        return result;
    }

    @Override
    public String calculateHv2(Element[] Edi) {
        String s = "";
        for (Element e : Edi) {
            s = s + e.toString();
        }
        String result = HashUtil.h2(s);
        return result;
    }

    @Override
    public JSONObject generateTx3Json(Element pii, Element[] pi, Element[] Ci, String[] mdi, String HV1, String HV2) {
        long ts = System.currentTimeMillis();
        Map<String, Object> map = new HashMap<>();
        map.put("Register", "Upload");
        map.put("pii", pii.toString());
        for (int i = 0; i < pi.length; i++) {
            map.put("pi" + Integer.toString(i + 1), pi[i].toString());
        }
        for (int i = 0; i < Ci.length; i++) {
            map.put("Ci" + Integer.toString(i + 1), Ci[i].toString());
        }
        for (int i = 0; i < mdi.length; i++) {
            map.put("mdi" + Integer.toString(i + 1), mdi[i]);
        }
        map.put("HV1", HV1);
        map.put("HV2", HV2);
        map.put("ts",Long.toString(ts));

        return new JSONObject(map);
    }

    @Override
    public JSONObject generateTx3Ecdsa(String privateKeyBase64, Element cri, String ts, JSONObject tx3Json) {
        TxEcdsaByCri tx3EcdsaByCri = null;
        try {
            System.out.println("签名私钥=" + privateKeyBase64);
            byte[] privateKey = DatatypeConverter.parseBase64Binary(privateKeyBase64);
            byte[] txEcdsaByteArray = ecdsa.jdkEcdsa1(privateKey, tx3Json.toJSONString());
            System.out.println("签名后的json_byte数租=" + Arrays.toString(txEcdsaByteArray));
            tx3EcdsaByCri = new TxEcdsaByCri(Base64Util.elementToBase64(cri), ts, DatatypeConverter.printBase64Binary(txEcdsaByteArray));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 生成Json
        return (JSONObject) JSONObject.toJSON(tx3EcdsaByCri);
    }

    @Override
    public List<EdBase64Json> getEdBase64Json() {
        return edMapper.getEdBase64Json();
    }

    @Override
    public EdBase64Json selectEdByTel(String tel) {
        return edMapper.selectEdByTel(tel);
    }

    @Override
    public void insertEd(EdBase64Json edBase64Json) {
         edMapper.insertEd(edBase64Json);
    }
}
