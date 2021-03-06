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
        System.out.println("==================????????????Tx5??????==================");

        Map<String, Object> map = new HashMap<>(1);
        map.put("username", username);
        System.out.println(map);
        List<DataUser> dataUserList = dataUserMapper.selectByMap(map);
        System.out.println("userModelList=" + dataUserList);

        //??????ski
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
            //  ???????????????
            Element[] attrs = new Element[attrsBase64.length];
            for (int i = 0; i < attrs.length; i++) {
                try {
                    attrs[i] = HashUtil.h1Zr(attrsBase64[i]);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

            //?????????????????????
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

            //??????TX5json
            JSONObject tx5Json = dataRetrievalService.generateTx5Json(au,proof);
            System.out.println("??????TX5json:" + tx5Json);

            //??????TX5
            TxEcdsa tx5Ecdsa = dataRetrievalService.generateTx5Ecdsa(tx5Json);

            //??????TxEcdsa??????Json
            JSONObject tx5EcdsaJson = (JSONObject) JSONObject.toJSON(tx5Ecdsa);
            System.out.println("tx5EcdsaJson=" + tx5EcdsaJson);

            // ???????????????Json
            Tx tx = new Tx(tx5Json.toJSONString(), tx5EcdsaJson.toJSONString());
            JSONObject txJson = (JSONObject) JSONObject.toJSON(tx);
            System.out.println("txJson=" + txJson);

            // ??????????????????
            String sr = HttpCallOtherInterfaceUtils.doPost("verifyTx", txJson, request);
            System.out.println("???????????????" + sr);

            if(Boolean.parseBoolean(sr)){
                boolean resultCri = dataUserEntityRegistration.verifyCriProof(proofCri, ri);
                boolean resultPii = dataUserEntityRegistration.verifyPiiProof(proofPii);
                boolean result = resultCri & resultPii;
                if (result){
                    System.out.println("?????????????????????");

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

                    System.out.println("==================??????????????????==================");
                    String url = "http://localhost:8091/toWeb3AddTx?showType=EunomiaCase&functionName=collect&criOrIndex=" + caseId + "&json=" + tx5Ecdsa.getTxEcdsaBase64()
                            + "&username=" + username1 + "&evidenceId=" + evidenceId1 + "&evidencePath=" + evidencePath1;

                    return "redirect:" + url;


                }else{
                    System.out.println("==================??????????????????==================");
                    String url = "http//localhost:8090/failure";
                    return "redirect:" + url;

                }
            }else {
                System.out.println("==================??????????????????==================");
                String url = "http//localhost:8090/failure";
                return "redirect:" + url;
            }

        }
        System.out.println("==================??????????????????==================");
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
        System.out.println("==================?????????????????????==================");

        long startTime = System.currentTimeMillis(); //??????????????????

        Element r = dataRetrievalService.getR();

        EdBase64 edBase64 = dataUploadService.readEd(evidenceId);
        System.out.println("edBase64" + edBase64);
        Ed ed = dataUploadService.getEd(edBase64);
        System.out.println("ED" + ed);

        MasterKey masterKey = dataUserEntityRegistration.readSKey(username);

        //??????msk
        Msk msk = dataUserEntityRegistration.readPkMsk(username).getMsk();

        //??????cri
        Map<String, Object> map = new HashMap<>(1);
        map.put("username", username);
        System.out.println(map);
        List<DataUser> dataUserList = dataUserMapper.selectByMap(map);
        System.out.println("userModelList=" + dataUserList);
        String criBase64 = dataUserList.get(0).getCri();
        System.out.println("cri="+criBase64);


        //??????prkj

        System.out.println("==================getKeyPair Start==================");
//        Map<String, Object> testMap = new HashMap<>(1);
//        testMap.put("cri", criBase64);
//        testMap.put("currentTimeMillis",currentTimeMillis);
//        System.out.println(currentTimeMillis);
//        JSONObject testJson = new JSONObject(testMap);
//        String sr = HttpCallOtherInterfaceUtils.doPost("getKeyPair", testJson, request);
//        System.out.println("???????????????" + sr);
//
//        // ?????????????????????
//        byte[] privateKey = DatatypeConverter.parseBase64Binary(sr);
//        System.out.println("????????????_byte??????=" + Arrays.toString(privateKey));
//        Element prk = Base64Util.base64ToElement(sr);
        Element prk = pairing.getZr().newRandomElement().getImmutable();

        System.out.println("==================getKeyPair Finish==================");

        //??????g1^r
        Element G1PowR = dataRetrievalService.generateG1PowR(r,msk).getImmutable();

        //??????prkBi
        int[] PrkBi = dataRetrievalService.generatePrkBi(prk);
        System.out.println(PrkBi.length);
        System.out.println("prkBi=" + Arrays.toString(PrkBi));

        //????????????Ri
        Element[] RandomRi = dataRetrievalService.generateRandomRi(PrkBi.length);
        System.out.println("ri=" + Arrays.toString(RandomRi));

        //?????? Ci ??? R
        Element[][] CR = dataRetrievalService.generateCommitment(PrkBi,RandomRi,G1PowR,msk);
        Element[] Ci = CR[0];
        Element[] Ri = CR[1];
        System.out.println("Ci=" + Arrays.toString(Ci));
        System.out.println("Ri=" + Arrays.toString(Ri));

        //????????????
        Element puk = msk.getG1().powZn(prk).getImmutable();

        //  ??????R
        Element R = pairing.getZr().newZeroElement().getImmutable();
        for (Element e : CR[1]) {
            R = R.add(e);
        }
        System.out.println("R=" + R);

        //?????? C
        Element C = dataRetrievalService.generateC(CR);
        System.out.println("C=" + C);

        //  ?????? C = g1^r * puk^r
        boolean resultA = dataRetrievalService.verifyC(C,R,puk,msk);
        System.out.println("C????????????=" + resultA);

        if(resultA){
            //  ???????????????
            boolean resultB = true;
            for (int i = 0; i < RandomRi.length; i++) {
                Element[] proof = dataRetrievalService.generateCiProof(G1PowR,RandomRi[i],PrkBi[i],msk);
                resultB = resultB & dataRetrievalService.verifyCiProof(G1PowR,proof,msk);
            }
            System.out.println("proof????????????=" + resultB);
            boolean result = resultA & resultB;
            //TODO ??????????????????
            if (result) {
                System.out.println("?????????????????????????????????");
                model.addAttribute("msg","?????????????????????????????????");

                //CPD??????Pi???????????? cpij
                String[][] P = dataRetrievalService.generatePi(Ci,r,msk);
//                String[][] ki = dataRetrievalService.generateKi(Ci,r,msk);
//                String[] ki0 = ki[0];
//                String[] ki1 = ki[1];
                String[] Ki0 = P[0];
                String[] Ki1 = P[1];
                System.out.println("Ki0" + Ki0);
                //cpij??????P'i,?????????CPD
                String[] k = dataRetrievalService.generateKi(G1PowR,RandomRi,Ki0,PrkBi);
                System.out.println("k="+k[0]);
                String[] PPi = dataRetrievalService.generatePpi(G1PowR,RandomRi,Ki0,PrkBi);
                boolean result2 = dataRetrievalService.verifyPpi(PPi,Ki1);
                System.out.println("?????????????????????" + result2);
                if(result2){
                    System.out.println("??????????????????????????????");
                    model.addAttribute("msg","??????????????????????????????");

                    boolean ki0 = dataRetrievalService.saveKi0(Ki0,evidenceId);
                    boolean ki1 = dataRetrievalService.saveKi1(Ki1,evidenceId);

                    boolean save = ki0 & ki1;
                    System.out.println("?????????????????????" + save);

                    //??????TX6json
                    JSONObject tx6Json = dataRetrievalService.generateTx6Json(caseId,Ci);
                    System.out.println("??????TX6json:" + tx6Json);

                    //??????TX6
                    TxEcdsa tx6Ecdsa = dataRetrievalService.generateTx6Ecdsa(tx6Json);

                    //??????TxEcdsa??????Json
                    JSONObject tx6EcdsaJson = (JSONObject) JSONObject.toJSON(tx6Ecdsa);
                    System.out.println("tx6EcdsaJson=" + tx6EcdsaJson);

                    // ???????????????Json
                    Tx tx = new Tx(tx6Json.toJSONString(), tx6EcdsaJson.toJSONString());
                    JSONObject txJson = (JSONObject) JSONObject.toJSON(tx);
                    System.out.println("txJson=" + txJson);

                    // ??????????????????
                    String sr = HttpCallOtherInterfaceUtils.doPost("verifyTx", txJson, request);
                    System.out.println("???????????????" + sr);

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

                    long endTime = System.currentTimeMillis(); //??????????????????

                    System.out.println("?????????????????????" + (endTime - startTime) + "ms"); //????????????????????????
                    String url = "http://localhost:8091/toWeb3AddTx?showType=EunomiaCase&functionName=permit&criOrIndex=" + caseId + "&json=" + tx6Ecdsa.getTxEcdsaBase64()
                            + "&username=" + username1 + "&evidenceId=" + evidenceId1 + "&evidencePath=" + evidencePath1;

                    return "redirect:" + url;

//                    JSONObject tx6Json = dataRetrievalService.generateTx6Json(String )
//                    System.out.println("tx2Json=" + tx6Json);

//                    byte[] EDi = dataRetrievalService.encrypt();
//                    String msg = "??????";
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
//                        model.addAttribute("msg","??????????????????????????????");
//                        String combinePath = dataRetrievalService.decWaterMask(path,Ki0);
//                        downloadFile(combinePath,response,model);
////                        model.addAttribute("msg","????????????");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }else {
                    System.out.println("???????????????????????????");
                    model.addAttribute("msg","???????????????????????????");
                }

            } else {
                System.out.println("?????????????????????");
                model.addAttribute("msg","?????????????????????");
            }
        }else {
            System.out.println("C????????????");
            model.addAttribute("msg","C????????????");
        }




        return "page-invoices";
    }

    @RequestMapping("/permit")
    public String dataPermit(String evidenceId,String evidencePath,
                             String caseId,
                             HttpServletRequest request,
                             HttpServletResponse response,Model model){

        long startTime = System.currentTimeMillis(); //??????????????????
        String[] Ki0 = dataRetrievalService.readKi0(evidenceId);
        String[] Ki1 = dataRetrievalService.readKi1(evidenceId);

        evidencePath = evidencePath.replaceAll(",","\\\\\\\\");
        System.out.println("evidencePath=" + evidencePath);

        String[][] path = dataRetrievalService.encWaterMask(evidencePath,Ki0,Ki1);

        String combinePath1 = null;
        try {
            model.addAttribute("msg","??????????????????????????????");
            String combinePath = dataRetrievalService.decWaterMask(path,Ki0);

            combinePath = combinePath.replaceAll("\\\\",",");
            System.out.println("combinePath=" + combinePath);

            combinePath1 = URLEncoder.encode(combinePath,"utf8");


//            downloadFile(combinePath,response,model);
//            model.addAttribute("msg","????????????");
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis(); //??????????????????

        System.out.println("?????????????????????" + (endTime - startTime) + "ms"); //????????????????????????


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
        //?????????????????????
        BufferedInputStream bis = null;
        OutputStream outputStream = null;

        try {
            outputStream = response.getOutputStream();
            //???????????????????????????????????????
            bis = new BufferedInputStream(new FileInputStream(new File(path)));
            int read = bis.read(buff);
            //??????while???????????????????????????????????????
            while (read != -1) {
                outputStream.write(buff, 0, buff.length);
                outputStream.flush();
                read = bis.read(buff);
            }
        } catch (IOException e) {
            e.printStackTrace();
            //??????????????????????????????????????????
            model.addAttribute("result","????????????");
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
        //???????????????????????????
        model.addAttribute("result","????????????");
        return "page-success";
    }
}
