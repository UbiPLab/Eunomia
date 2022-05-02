package com.tiantian.eunomia.watermark;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author shubham
 */
public class FileEncAndDec {
    //加密解密秘钥
    private static final int numOfEncAndDec = 0x99;
    //文件字节内容
    private static int dataOfFile = 0;
    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        String key = "0110000110010011001010111100010001101011100010000001011111010011010101101101001001011011010111011001110011100101010010101111001110000110011000100100111100100111101110110100010111001000001111011111101100100011010101001011111110100101110100000001100001100100";
        String hex = new BigInteger(key,2).toString(16);
//    	System.out.println(hex);
        byte byte_key[] = HexStringToBinary(hex);
        for(byte b:byte_key) {
            System.out.print(b+" ");
        }
        System.out.println();
        //初始文件
        File srcFile = new File("D:\\picture\\1.jpg");
        //加密文件
        File encFile = new File("D:\\picture\\encFile.tif");
        //解密文件
        File decFile = new File("D:\\picture\\decFile.jpg");

        try {
            //加密操作
            EncFile(srcFile, encFile,byte_key);
            DecFile(encFile,decFile,byte_key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
    }



    public static void EncFile(File srcFile, File encFile,byte[] byte_key) throws Exception {
        if(!srcFile.exists()){
//			System.out.println("source file not exixt");
            return;
        }

        if(!encFile.exists()){
//			System.out.println("encrypt file created");
            encFile.createNewFile();
        }
        InputStream fis  = new FileInputStream(srcFile);
        OutputStream fos = new FileOutputStream(encFile);
        int i = 0;
        while ((dataOfFile = fis.read()) > -1) {
            fos.write(dataOfFile^byte_key[i]);
            i = (i+1) % 32;

        }

        fis.close();
        fos.flush();
        fos.close();
    }

    public static void DecFile(File encFile, File decFile,byte[] byte_key) throws Exception {
        if(!encFile.exists()){
//			System.out.println("encrypt file not exixt");
            return;
        }

        if(!decFile.exists()){
//			System.out.println("decrypt file created");
            decFile.createNewFile();
        }

        InputStream fis  = new FileInputStream(encFile);
        OutputStream fos = new FileOutputStream(decFile);

        int i = 0;
        while ((dataOfFile = fis.read()) > -1) {
//			fos.write(dataOfFile^numOfEncAndDec);

            fos.write(dataOfFile^byte_key[i]);
            i = (i+1) % 32;
        }

        fis.close();
        fos.flush();
        fos.close();
    }

    public static byte[] HexStringToBinary(String hexString){

        if ((hexString == null) || (hexString.equals("")) || hexString.length()%2 != 0){
            return null;
        }else{
            hexString = hexString.toUpperCase();
            int length = hexString.length()/2;
            char[] bytec = hexString.toCharArray();
            byte[] bit = new byte[length];
            for (int i = 0; i < length; i++){
                int p = 2 * i;
                //两个十六进制字符转换成1个字节，第1个字符转换成byte后左移4位，然后和第2个字符的byte做或运算
                bit[i] = (byte) (fromCharToByte(bytec[p]) << 4 | fromCharToByte(bytec[p + 1]));
            }
            return bit;
        }
    }
    //字符转换为字节
    private static byte fromCharToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

}
