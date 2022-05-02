package com.tiantian.eunomia.controller;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.mapper.SystemInitMapper;
import com.tiantian.eunomia.mapper.attrs.PoliceStationMapper;
import com.tiantian.eunomia.mapper.attrs.StreetMapper;
import com.tiantian.eunomia.mapper.dataProvider.DataProviderInformationMapper;
import com.tiantian.eunomia.mapper.dataProvider.DataProviderMapper;
import com.tiantian.eunomia.mapper.dataUpload.*;
import com.tiantian.eunomia.model.Ed.Ed;
import com.tiantian.eunomia.model.Ed.EdBase64;
import com.tiantian.eunomia.model.Ed.EdBase64Json;
import com.tiantian.eunomia.model.PIi;
import com.tiantian.eunomia.model.SystemInit;
import com.tiantian.eunomia.model.TxByCri;
import com.tiantian.eunomia.model.attrs.PoliceStation;
import com.tiantian.eunomia.model.attrs.Street;
import com.tiantian.eunomia.model.dataUpload.DataUploadAttrs;
import com.tiantian.eunomia.model.dataUpload.Evidence;
import com.tiantian.eunomia.model.dataUpload.Picture;
import com.tiantian.eunomia.model.dataUpload.Video;
import com.tiantian.eunomia.model.pk.Pk;
import com.tiantian.eunomia.service.*;
import com.tiantian.eunomia.service.impl.*;
import com.tiantian.eunomia.utils.Base64Util;
import com.tiantian.eunomia.utils.HashUtil;
import com.tiantian.eunomia.utils.HttpCallOtherInterfaceUtils;
import com.tiantian.eunomia.utils.MD5Util;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author shubham
 */
@Controller
public class MyFileController {

    @Autowired
    DataUploadService dataUploadService = new DataUploadServiceImpl();

    @Autowired
    DataUserEntityRegistration dataUserEntityRegistration = new DataUserEntityRegistrationImpl();

    @Autowired
    DataProviderEntityRegistration dataProviderEntityRegistration = new DataProviderEntityRegistrationImpl();

    @Autowired
    DataProviderMapper dataProviderMapper;

    @Autowired
    DataProviderInformationMapper dataProviderInformationMapper;

    @Autowired
    DataUplaodAttrsMapper dataUplaodAttrsMapper;

    @Autowired
    PcitureMapper pcitureMapper;

    @Autowired
    VideoMapper videoMapper;

    @Autowired
    SystemInitMapper systemInitMapper;

    @Autowired
    StreetMapper streetMapper;

    @Autowired
    PoliceStationMapper policeStationMapper;

    @Autowired
    EvidenceMapper evidenceMapper;

    @Autowired
    TelMapper telMapper;


    private static final Pairing PAIRING = PairingFactory.getPairing("a1.properties");

        @RequestMapping(value = "/uploadFile", produces = "application/json;charset=UTF-8")
        public String uploadFile(@RequestParam MultipartFile file,
                                 @RequestParam String time,
                                 @RequestParam String location,
                                 @RequestParam String accidentType,
                                 @RequestParam String tel,
                                 Model model,
                                 HttpSession session,
                                 HttpServletRequest request) {

        System.out.print("上传文件===" + "\n");
        long startTime = System.currentTimeMillis(); //获取开始时间
        //判断文件是否为空
        if (file.isEmpty()) {
            return "上传文件不可为空";
        }

        // 获取文件名
        String fileName = file.getOriginalFilename();
        System.out.print("上传的文件名为: " + fileName + "\n");

        // 加个时间戳，尽量避免文件名称重复
        fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_" + fileName;
        System.out.print("（加个时间戳，尽量避免文件名称重复）保存的文件名为: " + fileName + "\n");

        // 文件的保存地址
        String path = "D:\\Idea Project\\xinan-competition\\evidence\\" + fileName;


        //文件绝对路径
        System.out.print("保存文件绝对路径" + path + "\n");

        //创建文件路径
        File dest = new File(path);

        //判断文件父目录是否存在
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdir();
        }

        String hash = "";
        int evidenceId = 0;
        int pictureId = 0;
        int videoId = 0;
        int fileId = 0;
        int edId = 0;


        Evidence evidence = new Evidence(0,time,location,accidentType,path);
        evidenceId = evidenceMapper.insert(evidence);
        int Id = evidence.getEvidenceId();
        String evdId = Integer.toString(Id);

        //获取pii
        String telCriBase64 = dataProviderEntityRegistration.readCri(tel);
        String cri = telCriBase64.replace("\"", "").replace("\"", "");
        Map<String, Object> map2 = new HashMap<>(1);
        map2.put("cri", cri);
        String piiBase64 = dataProviderMapper.selectByMap(map2).get(0).getPii();
        Element pii = Base64Util.base64ToElement(piiBase64);

        //获取所有Cri以及自己的Cri
        List<String> criBase64List = dataProviderMapper.getAllCri();
        Element telCri = Base64Util.base64ToElement(cri);
        Element telCriForZr = Base64Util.base64ToElementForZr(cri);

        //查找tel所对应的属性以及telCri所对应的pii,ri,rii
        String riiBase64 = dataProviderMapper.selectByMap(map2).get(0).getRii();
        String riBase64 = dataProviderMapper.selectByMap(map2).get(0).getRi();
        Element rii = Base64Util.base64ToElementForZr(riiBase64);
        Element ri = Base64Util.base64ToElementForZr(riBase64);

        Map<String, Object> map3 = new HashMap<>(1);
        map3.put("tel", tel);
        //构造attrs
        String[] attrsBase64 = new String[3];
        attrsBase64[0] = dataProviderInformationMapper.selectByMap(map3).get(0).getName();
        attrsBase64[1] = dataProviderInformationMapper.selectByMap(map3).get(0).getEmail();
        attrsBase64[2] = dataProviderInformationMapper.selectByMap(map3).get(0).getTel();
        Element[] attrs = new Element[attrsBase64.length];
        for (int i = 0; i < attrs.length; i++) {
            try {
                attrs[i] = HashUtil.h1Zr(attrsBase64[i]);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        //获取ski
        Element ski = dataProviderEntityRegistration.readSki(tel);

        //获取ci
        Element[] Ci = new Element[criBase64List.size()];
        for (int i = 0; i < criBase64List.size(); i++) {
            Ci[i] = Base64Util.base64ToElementForZr(criBase64List.get(i));
        }

        Map<String, Object> streetMap = new HashMap<>(1);
        streetMap.put("street_name",location);
        Street street = streetMapper.selectByMap(streetMap).get(0);
        int stationId = street.getPoliceStationId();

        Map<String, Object> stationMap = new HashMap<>(1);
        stationMap.put("police_station_id",stationId);
        PoliceStation policeStation = policeStationMapper.selectByMap(stationMap).get(0);
        String Station = policeStation.getPoliceStationName();

        String policeType = null;
        if(accidentType.equals("交通事故")){
            policeType = "交警";
        }else if (accidentType.equals("犯罪")){
            policeType = "刑警";
        }
        //生成mdi
        String[] mdi = new String[2];
//        mdi[0] = time;
        mdi[0] = Station;
        mdi[1] = policeType;


        //从本地拿PK
        Pk pk = dataUserEntityRegistration.readPkMsk(tel).getPk();
        Ed edi = dataUploadService.uploading(mdi, pk);
        EdBase64 edBase64 = dataUploadService.transformBase64(edi);
        String ediJson = dataUploadService.transform(edi);
        EdBase64Json edBase64Json = new EdBase64Json(0,evdId,ediJson);
//        dataUploadService.insertEd(edBase64Json);
        dataUploadService.saveEd(edBase64,evdId);
//        edBase64Mapper.insertEdBase64(edBase64);

        // 获取 N
        List<SystemInit> systemInits = systemInitMapper.selectAll();
        SystemInit systemInit = systemInits.get(systemInits.size() - 1);
        BigInteger N = Base64Util.base64ToBigInteger(systemInit.getN());

        //生成Ai和wi
        BigInteger Ai = dataProviderEntityRegistration.calculateAi(Ci, N);
        BigInteger wi = dataProviderEntityRegistration.calculateWi(Ci, telCriForZr, N);
        Element proofWi = PAIRING.getZr().newElement(wi);

        //零知识证明
        Element[] proofCri = dataUserEntityRegistration.generateCriProof(telCri, rii, ski, attrs);
        Element[] proofPii = dataUserEntityRegistration.generatePiiProof(pii, ri, ski);

        PIi pIi = new PIi(proofCri, proofPii, proofWi);

        //合成proof
        Element[] proofCp = ArrayUtils.addAll(proofCri, proofPii);
        List<Element> proofList = new ArrayList<>(proofCp.length);
        proofList.addAll(Arrays.asList(proofCp));
        proofList.add(proofWi);

        Element[] proof = new Element[proofList.size()];
        for (int i = 0; i < proofList.size(); i++) {
            proof[i] = proofList.get(i);
        }

        //生成HV1
        String HV1 = dataUploadService.calculateHv1(pii, proof, Ci, mdi);

        //生成HV2
        String HV2 = HashUtil.h2(ediJson);

        //生成TX3json
        JSONObject tx3Json = dataUploadService.generateTx3Json(pii, proof, Ci, mdi, HV1, HV2);
        System.out.println("生成TX3json:" + tx3Json);

        //生成TX3
        System.out.println("==================getKeyPair Start==================");
        Map<String, Object> getPrivateKeyMap = new HashMap<>(1);
        getPrivateKeyMap.put("cri", cri);
        String ts = Long.toString(System.currentTimeMillis());
        getPrivateKeyMap.put("ts",ts);
        JSONObject testJson = new JSONObject(getPrivateKeyMap);
        String getKeyPairResult = HttpCallOtherInterfaceUtils.doPost("getKeyPair", testJson, request);
        System.out.println("返回参数：" + getKeyPairResult);
        System.out.println("==================getKeyPair Finish==================");
        JSONObject tx3EcdsaByCriJson = dataUploadService.generateTx3Ecdsa(getKeyPairResult, Base64Util.base64ToElement(cri), ts , tx3Json);
        System.out.println("tx3EcdsaByCriJson=" + tx3EcdsaByCriJson);

        // 生成要发给区块链的完整json
        JSONObject tx3 = (JSONObject) JSONObject.toJSON(new TxByCri(tx3Json.toJSONString(), tx3EcdsaByCriJson.toJSONString()));
        System.out.println("tx3=" + tx3);

        String sr = HttpCallOtherInterfaceUtils.doPost("verifyTx3", tx3, request);
        System.out.println("返回参数：" + sr);

        try {
            //保存数据属性
            DataUploadAttrs dataUploadAttrs = new DataUploadAttrs(0, time, location, accidentType);
            int dataAttrsResult = dataUplaodAttrsMapper.insert(dataUploadAttrs);
            System.out.println("dataAttrs插入结果 = " + dataAttrsResult);

            //上传文件
            file.transferTo(dest); //保存文件
            System.out.println("保存文件路径" + path + "\n");
            String url = "http://localhost:8090/images/" + fileName;
            hash = MD5Util.md5HashCode(path);
            System.out.println(hash);

            Picture picture = new Picture(0, url, fileName, path, hash);
            Video video = new Video(0, url, fileName, path, hash);

            if (fileName.split("\\.")[1].equals("mp4") ||
                    fileName.split("\\.")[1].equals("mov") ||
                    fileName.split("\\.")[1].equals("flv")) {
                videoId = videoMapper.insert(video);
            } else if (fileName.split("\\.")[1].equals("png") || fileName.split("\\.")[1].equals("jpg") || fileName.split("\\.")[1].equals("jpeg")) {
                pictureId = pcitureMapper.insert(picture);
            } else {
                fileId = 0;
            }

        } catch (IOException e) {
            model.addAttribute("failure", "failure");
            if (fileName.split("\\.")[1].equals("mp4") ||
                    fileName.split("\\.")[1].equals("mov") ||
                    fileName.split("\\.")[1].equals("flv")) {
                return "index";
            } else if (fileName.split("\\.")[1].equals("png") ||
                    fileName.split("\\.")[1].equals("jpg") ||
                    fileName.split("\\.")[1].equals("jpeg")) {
                return "index";
            } else {
                return "index";
            }
        }

        model.addAttribute("success", "success");
        model.addAttribute("result", "result");
        session.setAttribute("hash", "hash");
        String url = "http://localhost:8091/toWeb3AddTx?showType=cri&functionName=addEvidence&criOrIndex=" + evdId + "&json=" + tx3EcdsaByCriJson;

        long endTime = System.currentTimeMillis(); //获取结束时间

            System.out.println("程序运行时间：" + (endTime - startTime) + "ms"); //输出程序运行时间
            return "redirect:" + url;
//            return "index";

//        if (fileName.split("\\.")[1].equals("mp4") ||
//                fileName.split("\\.")[1].equals("mov") ||
//                fileName.split("\\.")[1].equals("flv")) {
//
//
//        } else if (fileName.split("\\.")[1].equals("png") || fileName.split("\\.")[1].equals("jpg") || fileName.split("\\.")[1].equals("jpeg")) {
//
//            long endTime = System.currentTimeMillis(); //获取结束时间
//
//            System.out.println("程序运行时间：" + (endTime - startTime) + "ms"); //输出程序运行时间
//            return "redirect:http://localhost:8080/?Id=" + pictureId + "&hash=" + hash;
//        } else {
//            long endTime = System.currentTimeMillis(); //获取结束时间
//
//            System.out.println("程序运行时间：" + (endTime - startTime) + "ms"); //输出程序运行时间
//            return "index";
//        }
    }


    @RequestMapping("/getNumber")
    @ResponseBody
    public int getNumber(@RequestParam("tel") String tel){
            int number = telMapper.getNumber(tel);
//            String numbers = Integer.toString(number);
            return number;
    }
}
