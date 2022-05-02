package com.tiantian.eunomia.controller;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.mapper.DataUserAttrsMapper;
import com.tiantian.eunomia.mapper.DataUserMapper;
import com.tiantian.eunomia.model.Ed.Ed;
import com.tiantian.eunomia.model.Ed.EdBase64;
import com.tiantian.eunomia.model.MasterKey.MasterKey;
import com.tiantian.eunomia.model.PIj;
import com.tiantian.eunomia.model.Tx;
import com.tiantian.eunomia.model.TxEcdsa;
import com.tiantian.eunomia.model.dataUser.DataUser;
import com.tiantian.eunomia.model.dataUser.DataUserAttrs;
import com.tiantian.eunomia.model.msk.Msk;
import com.tiantian.eunomia.model.pk.Pk;
import com.tiantian.eunomia.service.DataProviderEntityRegistration;
import com.tiantian.eunomia.service.DataRetrievalService;
import com.tiantian.eunomia.service.DataUploadService;
import com.tiantian.eunomia.service.DataUserEntityRegistration;
import com.tiantian.eunomia.service.impl.DataProviderEntityRegistrationImpl;
import com.tiantian.eunomia.service.impl.DataRetrievalServiceImpl;
import com.tiantian.eunomia.service.impl.DataUploadServiceImpl;
import com.tiantian.eunomia.utils.Base64Util;
import com.tiantian.eunomia.utils.FileUtil;
import com.tiantian.eunomia.utils.HashUtil;
import com.tiantian.eunomia.utils.HttpCallOtherInterfaceUtils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author shubham
 */
@Controller
public class DataRetrievalController {

    private final String currentTimeMillis = String.valueOf(System.currentTimeMillis());
    public static Pairing pairing = PairingFactory.getPairing("a1.properties");
//    public static Element r = pairing.getZr().newRandomElement().getImmutable();

    public static int Len = 160;
    @Autowired
    DataRetrievalService dataRetrievalService = new DataRetrievalServiceImpl();

    @Autowired
    DataUserAttrsMapper dataUserAttrsMapper;

    @Autowired
    DataUserEntityRegistration dataUserEntityRegistration;

    @Autowired
    DataUserMapper dataUserMapper;

    @Autowired
    DataProviderEntityRegistration dataProviderEntityRegistration = new DataProviderEntityRegistrationImpl();

    @Autowired
    DataUploadService dataUploadService = new DataUploadServiceImpl();

    @RequestMapping("/dataRetrieval")
    public String dataretrieval(@RequestParam("username") String username,
                                @RequestParam("caseId") String caseId,
                                @RequestParam("evidenceId") String evidenceId,
                                @RequestParam("evidencePath") String evidencePath,
                                HttpServletRequest request,Model model){
        System.out.println("==================数据检索Tx5开始==================");

        Map<String, Object> map = new HashMap<>(1);
        map.put("username", username);
        System.out.println(map);
        List<DataUser> dataUserList = dataUserMapper.selectByMap(map);
        System.out.println("userModelList=" + dataUserList);

        //获取ski
        Element ski = dataProviderEntityRegistration.readSki(username);

        if (dataUserList != null){
            String criBase64 = dataUserList.get(0).getCri();
            String piiBase64 = dataUserList.get(0).getPii();
            String riBase64 = dataUserList.get(0).getRi();
            String riiBase64 = dataUserList.get(0).getRii();

            Element cri = Base64Util.base64ToElement(criBase64);
            Element pii = Base64Util.base64ToElement(piiBase64);
            Element ri = Base64Util.base64ToElementForZr(riBase64);
            Element rii = Base64Util.base64ToElementForZr(riiBase64);

            int dataUserId = dataUserList.get(0).getDataUserId();
            System.out.println("dataUserId=" + dataUserId);
            Map<String, Object> attrMap = new HashMap<>(1);
            attrMap.put("data_user_id", dataUserId);
            DataUserAttrs dataUserAttrs = dataUserAttrsMapper.selectByMap(attrMap).get(0);
            System.out.println("dataUserAttrs=" + dataUserAttrs);

            String[] attrsBase64 = new String[3];
            attrsBase64[0] = dataUserAttrs.getPoliceNumber();
            attrsBase64[1] = dataUserAttrs.getPoliceType();
            attrsBase64[2] = dataUserAttrs.getPoliceStation();
            //  构造属性集
            Element[] attrs = new Element[attrsBase64.length];
            for (int i = 0; i < attrs.length; i++) {
                try {
                    attrs[i] = HashUtil.h1Zr(attrsBase64[i]);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

            //生成零知识证明
            Element[] proofCri = dataUserEntityRegistration.generateCriProof(cri, rii, ski, attrs);
            Element[] proofPii = dataUserEntityRegistration.generatePiiProof(pii, ri, ski);

            PIj pIj = new PIj(proofCri,proofPii);

            Element[] proofCp = ArrayUtils.addAll(pIj.getProof_cri(),pIj.getProof_pii());
            List<Element> proofList = new ArrayList<>(proofCp.length);
            proofList.addAll(Arrays.asList(proofCp));

            Element[] proof = new Element[proofList.size()];
            for (int i = 0; i < proofList.size(); i++) {
                proof[i] = proofList.get(i);
            }

            Map<String, Object> map2 = new HashMap<>(1);
            map2.put("username", username);
            int userId = dataUserMapper.selectByMap(map2).get(0).getDataUserId();

            Map<String, Object> map3 = new HashMap<>(1);
            map3.put("data_user_id", userId);
            String au = dataUserAttrsMapper.selectByMap(map3).get(0).getPoliceStation();

            //生成TX5json
            JSONObject tx5Json = dataRetrievalService.generateTx5Json(au,proof);
            System.out.println("生成TX5json:" + tx5Json);

            //生成TX5
            TxEcdsa tx5Ecdsa = dataRetrievalService.generateTx5Ecdsa(tx5Json);

            //生成TxEcdsa对象Json
            JSONObject tx5EcdsaJson = (JSONObject) JSONObject.toJSON(tx5Ecdsa);
            System.out.println("tx5EcdsaJson=" + tx5EcdsaJson);

            // 生成总数据Json
            Tx tx = new Tx(tx5Json.toJSONString(), tx5EcdsaJson.toJSONString());
            JSONObject txJson = (JSONObject) JSONObject.toJSON(tx);
            System.out.println("txJson=" + txJson);

            // 发送给区块链
            String sr = HttpCallOtherInterfaceUtils.doPost("verifyTx", txJson, request);
            System.out.println("返回参数：" + sr);

            if(Boolean.parseBoolean(sr)){
                boolean resultCri = dataUserEntityRegistration.verifyCriProof(proofCri, ri);
                boolean resultPii = dataUserEntityRegistration.verifyPiiProof(proofPii);
                boolean result = resultCri & resultPii;
                if (result){
                    System.out.println("零知识证明通过");

                    evidencePath = evidencePath.replaceAll("\\\\",",");
                    System.out.println("evidencePath=" + evidencePath);

                    String evidenceId1 = null;
                    String evidencePath1 = null;
                    String username1 = null;

                    try {
                        evidenceId1 = URLEncoder.encode(evidenceId,"utf8");
                        evidencePath1 = URLEncoder.encode(evidencePath,"utf8");
                        username1 = URLEncoder.encode(username,"utf8");

                        System.out.println();
                        System.out.println("evidencePath=" + evidencePath1);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    System.out.println("==================数据检索结束==================");
                    String url = "http://localhost:8091/toWeb3AddTx?showType=EunomiaCase&functionName=collect&criOrIndex=" + caseId + "&json=" + tx5Ecdsa.getTxEcdsaBase64()
                            + "&username=" + username1 + "&evidenceId=" + evidenceId1 + "&evidencePath=" + evidencePath1;

                    return "redirect:" + url;


                }else{
                    System.out.println("==================数据检索失败==================");
                    String url = "http//localhost:8090/failure";
                    return "redirect:" + url;

                }
            }else {
                System.out.println("==================数据检索失败==================");
                String url = "http//localhost:8090/failure";
                return "redirect:" + url;
            }

        }
        System.out.println("==================数据检索失败==================");
        String url = "http//localhost:8090/failure";
        return "redirect:" + url;

    }

    @RequestMapping("/oblivious")
    public String dataAnalysis(@RequestParam("username") String username,
                               @RequestParam("caseId") String caseId,
                               @RequestParam("evidenceId") String evidenceId,
                               @RequestParam("evidencePath") String evidencePath,
                               HttpServletRequest request,HttpServletResponse response,Model model){

//        dataretrieval(username);
        System.out.println("==================不经意传输开始==================");

        long startTime = System.currentTimeMillis(); //获取开始时间

        Element r = dataRetrievalService.getR();

        EdBase64 edBase64 = dataUploadService.readEd(evidenceId);
        System.out.println("edBase64" + edBase64);
        Ed ed = dataUploadService.getEd(edBase64);
        System.out.println("ED" + ed);

        MasterKey masterKey = dataUserEntityRegistration.readSKey(username);

        //获取msk
        Msk msk = dataUserEntityRegistration.readPkMsk(username).getMsk();

        //获取cri
        Map<String, Object> map = new HashMap<>(1);
        map.put("username", username);
        System.out.println(map);
        List<DataUser> dataUserList = dataUserMapper.selectByMap(map);
        System.out.println("userModelList=" + dataUserList);
        String criBase64 = dataUserList.get(0).getCri();
        System.out.println("cri="+criBase64);


        //获取prkj

        System.out.println("==================getKeyPair Start==================");
//        Map<String, Object> testMap = new HashMap<>(1);
//        testMap.put("cri", criBase64);
//        testMap.put("currentTimeMillis",currentTimeMillis);
//        System.out.println(currentTimeMillis);
//        JSONObject testJson = new JSONObject(testMap);
//        String sr = HttpCallOtherInterfaceUtils.doPost("getKeyPair", testJson, request);
//        System.out.println("返回参数：" + sr);
//
//        // 处理收到的私钥
//        byte[] privateKey = DatatypeConverter.parseBase64Binary(sr);
//        System.out.println("签名私钥_byte数租=" + Arrays.toString(privateKey));
//        Element prk = Base64Util.base64ToElement(sr);
        Element prk = pairing.getZr().newRandomElement().getImmutable();

        System.out.println("==================getKeyPair Finish==================");

        //生成g1^r
        Element G1PowR = dataRetrievalService.generateG1PowR(r,msk).getImmutable();

        //生成prkBi
        int[] PrkBi = dataRetrievalService.generatePrkBi(prk);
        System.out.println(PrkBi.length);
        System.out.println("prkBi=" + Arrays.toString(PrkBi));

        //随机生成Ri
        Element[] RandomRi = dataRetrievalService.generateRandomRi(PrkBi.length);
        System.out.println("ri=" + Arrays.toString(RandomRi));

        //计算 Ci 和 R
        Element[][] CR = dataRetrievalService.generateCommitment(PrkBi,RandomRi,G1PowR,msk);
        Element[] Ci = CR[0];
        Element[] Ri = CR[1];
        System.out.println("Ci=" + Arrays.toString(Ci));
        System.out.println("Ri=" + Arrays.toString(Ri));

        //计算公钥
        Element puk = msk.getG1().powZn(prk).getImmutable();

        //  计算R
        Element R = pairing.getZr().newZeroElement().getImmutable();
        for (Element e : CR[1]) {
            R = R.add(e);
        }
        System.out.println("R=" + R);

        //计算 C
        Element C = dataRetrievalService.generateC(CR);
        System.out.println("C=" + C);

        //  验证 C = g1^r * puk^r
        boolean resultA = dataRetrievalService.verifyC(C,R,puk,msk);
        System.out.println("C验证结果=" + resultA);

        if(resultA){
            //  零知识证明
            boolean resultB = true;
            for (int i = 0; i < RandomRi.length; i++) {
                Element[] proof = dataRetrievalService.generateCiProof(G1PowR,RandomRi[i],PrkBi[i],msk);
                resultB = resultB & dataRetrievalService.verifyCiProof(G1PowR,proof,msk);
            }
            System.out.println("proof验证结果=" + resultB);
            boolean result = resultA & resultB;
            //TODO 拆成两个函数
            if (result) {
                System.out.println("数据检索零知识证明成功");
                model.addAttribute("msg","数据检索零知识证明成功");

                //CPD计算Pi，发送给 cpij
                String[][] P = dataRetrievalService.generatePi(Ci,r,msk);
//                String[][] ki = dataRetrievalService.generateKi(Ci,r,msk);
//                String[] ki0 = ki[0];
//                String[] ki1 = ki[1];
                String[] Ki0 = P[0];
                String[] Ki1 = P[1];
                System.out.println("Ki0" + Ki0);
                //cpij计算P'i,发送给CPD
                String[] k = dataRetrievalService.generateKi(G1PowR,RandomRi,Ki0,PrkBi);
                System.out.println("k="+k[0]);
                String[] PPi = dataRetrievalService.generatePpi(G1PowR,RandomRi,Ki0,PrkBi);
                boolean result2 = dataRetrievalService.verifyPpi(PPi,Ki1);
                System.out.println("不经意传输验证" + result2);
                if(result2){
                    System.out.println("不经意传输验证成功！");
                    model.addAttribute("msg","不经意传输验证成功！");

                    boolean ki0 = dataRetrievalService.saveKi0(Ki0,evidenceId);
                    boolean ki1 = dataRetrievalService.saveKi1(Ki1,evidenceId);

                    boolean save = ki0 & ki1;
                    System.out.println("是否保存成功：" + save);

                    //生成TX6json
                    JSONObject tx6Json = dataRetrievalService.generateTx6Json(caseId,Ci);
                    System.out.println("生成TX6json:" + tx6Json);

                    //生成TX6
                    TxEcdsa tx6Ecdsa = dataRetrievalService.generateTx6Ecdsa(tx6Json);

                    //生成TxEcdsa对象Json
                    JSONObject tx6EcdsaJson = (JSONObject) JSONObject.toJSON(tx6Ecdsa);
                    System.out.println("tx6EcdsaJson=" + tx6EcdsaJson);

                    // 生成总数据Json
                    Tx tx = new Tx(tx6Json.toJSONString(), tx6EcdsaJson.toJSONString());
                    JSONObject txJson = (JSONObject) JSONObject.toJSON(tx);
                    System.out.println("txJson=" + txJson);

                    // 发送给区块链
                    String sr = HttpCallOtherInterfaceUtils.doPost("verifyTx", txJson, request);
                    System.out.println("返回参数：" + sr);

                    String evidenceId1 = null;
                    String evidencePath1 = null;
                    String username1 = null;

                    try {
                        evidenceId1 = URLEncoder.encode(evidenceId,"utf8");
                        evidencePath1 = URLEncoder.encode(evidencePath,"utf8");
                        username1 = URLEncoder.encode(username,"utf8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    long endTime = System.currentTimeMillis(); //获取结束时间

                    System.out.println("程序运行时间：" + (endTime - startTime) + "ms"); //输出程序运行时间
                    String url = "http://localhost:8091/toWeb3AddTx?showType=EunomiaCase&functionName=permit&criOrIndex=" + caseId + "&json=" + tx6Ecdsa.getTxEcdsaBase64()
                            + "&username=" + username1 + "&evidenceId=" + evidenceId1 + "&evidencePath=" + evidencePath1;

                    return "redirect:" + url;

//                    JSONObject tx6Json = dataRetrievalService.generateTx6Json(String )
//                    System.out.println("tx2Json=" + tx6Json);

//                    byte[] EDi = dataRetrievalService.encrypt();
//                    String msg = "你好";
//                    Element di = ed.getDi();
//                    byte[] aesBuf = dataRetrievalService.encrypt(di,msg);
//
//                    byte[] byte_msg = dataRetrievalService.decrypt(masterKey,ed,aesBuf);
////                    String message = new String(byte_msg);
//                    System.out.println(di);
//                    System.out.println(message);
//                    String picturePath = "D:\\Idea Project\\xinan-competition\\evidence\\" + evidencePath;
//                    String[][] path = dataRetrievalService.encWaterMask(evidencePath,Ki0,Ki1);
//                    dataRetrievalService.decWaterMask(path,k);
//                    try {
//                        model.addAttribute("msg","正在进行不经意传输！");
//                        String combinePath = dataRetrievalService.decWaterMask(path,Ki0);
//                        downloadFile(combinePath,response,model);
////                        model.addAttribute("msg","下载成功");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }else {
                    System.out.println("不经意传输验证失败");
                    model.addAttribute("msg","不经意传输验证失败");
                }

            } else {
                System.out.println("零知识证明失败");
                model.addAttribute("msg","零知识证明失败");
            }
        }else {
            System.out.println("C验证失败");
            model.addAttribute("msg","C验证失败");
        }




        return "page-invoices";
    }

    @RequestMapping("/permit")
    public String dataPermit(String evidenceId,String evidencePath,
                             String caseId,
                             HttpServletRequest request,
                             HttpServletResponse response,Model model){

        long startTime = System.currentTimeMillis(); //获取开始时间
        String[] Ki0 = dataRetrievalService.readKi0(evidenceId);
        String[] Ki1 = dataRetrievalService.readKi1(evidenceId);

        evidencePath = evidencePath.replaceAll(",","\\\\\\\\");
        System.out.println("evidencePath=" + evidencePath);

        String[][] path = dataRetrievalService.encWaterMask(evidencePath,Ki0,Ki1);

        String combinePath1 = null;
        try {
            model.addAttribute("msg","正在进行不经意传输！");
            String combinePath = dataRetrievalService.decWaterMask(path,Ki0);

            combinePath = combinePath.replaceAll("\\\\",",");
            System.out.println("combinePath=" + combinePath);

            combinePath1 = URLEncoder.encode(combinePath,"utf8");


//            downloadFile(combinePath,response,model);
//            model.addAttribute("msg","下载成功");
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis(); //获取结束时间

        System.out.println("程序运行时间：" + (endTime - startTime) + "ms"); //输出程序运行时间


        String url = "http://localhost:8091/toWeb3AddTx?showType=EunomiaCase&functionName=analyze&criOrIndex=" + caseId + "&json=" + ""
                 + "&path=" + combinePath1;

        return "redirect:" + url;
    }

    @RequestMapping("/alter")
    public String alter(String path, Model model){
        path = path.replaceAll(",","\\\\");
        System.out.println("path:" + path);

        model.addAttribute("path",path);
        return "page-success";
    }

    @RequestMapping( "/downloadFile")
    public String downloadFile(String path, HttpServletResponse response, Model model){

//        path = path.replaceAll(",","\\\\");

        System.out.println("path:" + path);

        String fileName = "combine.jpg";
        if(fileName.split("\\.")[1].equals("png")){
            response.setHeader("content-type","image/png");
            response.setContentType("application/png");
            response.setHeader("Content-Disposition","attachment; filename=" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +".png");
        }else if(fileName.split("\\.")[1].equals("jpg") ||
                fileName.split("\\.")[1].equals("jpeg")||fileName.split("\\.")[2].equals("JPG")){
            response.setHeader("content-type","image/jpeg");
            response.setContentType("application/jpeg");
            response.setHeader("Content-Disposition","attachment; filename=" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +".jpg");
        }
        byte[] buff = new byte[1024];
        //创建缓冲输入流
        BufferedInputStream bis = null;
        OutputStream outputStream = null;

        try {
            outputStream = response.getOutputStream();
            //这个路径为待下载文件的路径
            bis = new BufferedInputStream(new FileInputStream(new File(path)));
            int read = bis.read(buff);
            //通过while循环写入到指定了的文件夹中
            while (read != -1) {
                outputStream.write(buff, 0, buff.length);
                outputStream.flush();
                read = bis.read(buff);
            }
        } catch (IOException e) {
            e.printStackTrace();
            //出现异常返回给页面失败的信息
            model.addAttribute("result","下载失败");
        }finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //成功后返回成功信息
        model.addAttribute("result","下载成功");
        return "page-success";
    }
}
