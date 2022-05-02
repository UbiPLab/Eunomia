package com.tiantian.eunomia.controller;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.mapper.dataUpload.EvidenceMapper;
import com.tiantian.eunomia.mapper.dataUpload.TelMapper;
import com.tiantian.eunomia.model.dataUpload.Evidence;
import com.tiantian.eunomia.model.dataUpload.Tel;
import com.tiantian.eunomia.utils.HttpCallOtherInterfaceUtils;
import com.tiantian.eunomia.watermark.Combinepic;
import com.tiantian.eunomia.watermark.FileEncAndDec;
import com.tiantian.eunomia.watermark.ImageWaterMarkMain;
import com.tiantian.eunomia.watermark.Splitpic;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tiantian
 */
@Controller
@RequestMapping("/test")
public class TestController {

    TelMapper telMapper;

    EvidenceMapper evidenceMapper;

//    @RequestMapping("/getNumber")
//    public String getNumber(){
//
//        int number=evidenceMapper.count();
//        String numbers = Integer.toString(number);
//        return numbers;
//    }

    @RequestMapping("/getDataProviderCi")
    public String getDataProviderCi(HttpServletRequest request) {

        System.out.println("==================getDataProviderCi Start==================");
        Map<String, Object> testMap = new HashMap<>(1);
        testMap.put("test", "test");
        JSONObject testJson = new JSONObject(testMap);
        String sr = HttpCallOtherInterfaceUtils.doPost("getDataProviderCi", testJson, request);
        System.out.println("返回参数：" + sr);

        System.out.println("==================getDataProviderCi Finish==================");
        return "index";
    }

    @RequestMapping("/getKeyPair")
    public String getKeyPair(HttpServletRequest request) {

        System.out.println("==================getKeyPair Start==================");
        Map<String, Object> testMap = new HashMap<>(1);
        testMap.put("cri", "cri");
        JSONObject testJson = new JSONObject(testMap);
        String sr = HttpCallOtherInterfaceUtils.doPost("getKeyPair", testJson, request);
        System.out.println("返回参数：" + sr);

        // 处理收到的私钥
        byte[] privateKey = DatatypeConverter.parseBase64Binary(sr);
        System.out.println("签名私钥_byte数租=" + Arrays.toString(privateKey));

        System.out.println("==================getKeyPair Finish==================");
        return "index";
    }

    @RequestMapping("/waterMask")
    public String waterMask(@RequestParam("a") String a,HttpServletRequest request){
        String key = "0110000110010011001010111100010001101011100010000001011111010011010101101101001001011011010111011001110011100101010010101111001110000110011000100100111100100111101110110100010111001000001111011111101100100011010101001011111110100101110100000001100001100100";
        String hex = new BigInteger(key,2).toString(16);
//    	System.out.println(hex);
        byte byte_key[] = FileEncAndDec.HexStringToBinary(hex);
//        System.out.println(path);
        long startTime = System.currentTimeMillis();
        try {
            Splitpic.split_picture("D:\\picture\\1.jpeg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageWaterMarkMain.watermark_in();
        for(int i=0;i<160;i++) {
            String path0 = "D:\\picture\\split1\\img0 "+i+".bmp";
            String path1 = "D:\\picture\\split1\\img1 "+i+".bmp";
            String encpath0 = "D:\\picture\\enc\\img0 "+i+"enc.tif";
            String encpath1 = "D:\\picture\\enc\\img1 "+i+"enc.tif";
            try {
                FileEncAndDec.EncFile(new File(path0), new File(encpath0),byte_key);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                FileEncAndDec.EncFile(new File(path1), new File(encpath1),byte_key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(a);
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间1：" + (endTime - startTime) + "ms");
        long startTime2 = System.currentTimeMillis();
        for(int i=0;i<160;i++) {
            String encpath0 = "D:\\picture\\enc\\img0 "+i+"enc.tif";
            String encpath1 = "D:\\picture\\enc\\img1 "+i+"enc.tif";
            String decpath0 = "D:\\picture\\dec\\img0 "+i+".bmp";
            String decpath1 = "D:\\picture\\dec\\img1 "+i+".bmp";
            try {
                FileEncAndDec.DecFile(new File(encpath0), new File(decpath0),byte_key);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //FileEncAndDec.DecFile(new File(encpath1), new File(decpath1),byte_key);
        }
        System.out.println("解密成功");
        try {
            Combinepic.combine_picture();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime2 = System.currentTimeMillis();
        System.out.println("程序运行时间2：" + (endTime2 - startTime2) + "ms");

        return "index";
    }





}