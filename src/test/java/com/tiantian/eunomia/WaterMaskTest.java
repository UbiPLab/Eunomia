package com.tiantian.eunomia;

import com.tiantian.eunomia.watermark.Combinepic;
import com.tiantian.eunomia.watermark.FileEncAndDec;
import com.tiantian.eunomia.watermark.ImageWaterMarkMain;
import com.tiantian.eunomia.watermark.Splitpic;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;

public class WaterMaskTest {
    public static void main(String[] args) throws Exception{
        String key = "0110000110010011001010111100010001101011100010000001011111010011010101101101001001011011010111011001110011100101010010101111001110000110011000100100111100100111101110110100010111001000001111011111101100100011010101001011111110100101110100000001100001100100";
        String hex = new BigInteger(key,2).toString(16);
//    	System.out.println(hex);
        byte byte_key[] = FileEncAndDec.HexStringToBinary(hex);
        long startTime = System.currentTimeMillis();
        Splitpic.split_picture("D:\\picture\\1.jpeg");
        ImageWaterMarkMain.watermark_in();
        for(int i=0;i<160;i++) {
//            String key0 = ki0[i];
//            String key1 = ki1[i];
//            String hex0 = new BigInteger(key0,2).toString(16);
//            String hex1 = new BigInteger(key1,2).toString(16);
//            byte[] byte_key0 = FileEncAndDec.HexStringToBinary(hex0);
//            byte[] byte_key1 = FileEncAndDec.HexStringToBinary(hex1);
            String path0 = "D:\\picture\\split1\\img0 "+i+".bmp";
            String path1 = "D:\\picture\\split1\\img1 "+i+".bmp";
            String encpath0 = "D:\\picture\\enc\\img0 "+i+"enc.tif";
            String encpath1 = "D:\\picture\\enc\\img1 "+i+"enc.tif";
            System.out.println("byte-key=" + Arrays.toString(byte_key));
            FileEncAndDec.EncFile(new File(path0), new File(encpath0),byte_key);
            FileEncAndDec.EncFile(new File(path1), new File(encpath1),byte_key);
        }
        System.out.println("加密成功");
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间1：" + (endTime - startTime) + "ms");
        long startTime2 = System.currentTimeMillis();
        for(int i=0;i<160;i++) {
            String encpath0 = "D:\\picture\\enc\\img0 "+i+"enc.tif";
            String encpath1 = "D:\\picture\\enc\\img1 "+i+"enc.tif";
            String decpath0 = "D:\\picture\\dec\\img0 "+i+".bmp";
            String decpath1 = "D:\\picture\\dec\\img1 "+i+".bmp";
//            System.out.println("byte-key=" + Arrays.toString(byte_key));
            FileEncAndDec.DecFile(new File(encpath0), new File(decpath0),byte_key);
            //FileEncAndDec.DecFile(new File(encpath1), new File(decpath1),byte_key);
        }
        System.out.println("解密成功");
        Combinepic.combine_picture();
        long endTime2 = System.currentTimeMillis();
        System.out.println("程序运行时间2：" + (endTime2 - startTime2) + "ms");
    }
}
