package com.tiantian.eunomia.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * @author tiantian152
 */
public class FileUtil {

    /**
     * String数组转换为Byte数组
     * @param strs
     * @return
     */
    public static byte[] serialize(String[] strs) {
        ArrayList<Byte> byteList = new ArrayList<Byte>();
        for (String str: strs) {
            int len = str.getBytes().length;
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(len);
            byte[] lenArray = bb.array();
            for (byte b: lenArray) {
                byteList.add(b);
            }
            byte[] strArray = str.getBytes();
            for (byte b: strArray) {
                byteList.add(b);
            }
        }
        byte[] result = new byte[byteList.size()];
        for (int i=0; i<byteList.size(); i++) {
            result[i] = byteList.get(i);
        }
        return result;
    }

    /**
     * Byte数组转换为String数组
     * @param bytes
     * @return
     */
    public static String[] unserialize(byte[] bytes) {
        ArrayList<String> strList = new ArrayList<String>();
        for (int i=0; i< bytes.length;) {
            byte[] lenArray = new byte[4];
            for (int j=i; j<i+4; j++) {
                lenArray[j-i] = bytes[j];
            }
            ByteBuffer wrapped = ByteBuffer.wrap(lenArray);
            int len = wrapped.getInt();
            byte[] strArray = new byte[len];
            for (int k=i+4; k<i+4+len; k++) {
                strArray[k-i-4] = bytes[k];
            }
            strList.add(new String(strArray));
            i += 4+len;
        }
        return strList.toArray(new String[strList.size()]);
    }

    /**
     * @param byteArray 需要转换成文件的byte数组
     * @param filePath  生成的文件保存路径
     * @param fileName  生成文件后保存的名称如 test.pdf，test.jpg等
     */
    public static boolean byteArrayToFile(byte[] byteArray, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file;
        try {
            File dir = new File(filePath);
            boolean isDir = dir.isDirectory();
            // 目录不存在则先建目录
            if (!isDir) {
                try {
                    boolean mkdirs = dir.mkdirs();
                    System.out.println("创建目录:" + mkdirs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(byteArray);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * 读取文件
     */
    public static String readFile(String filePath, String fileName) {
        BufferedReader reader = null;
        String laststr = "";
        try {
            String path = filePath + File.separator + fileName;
            FileInputStream fileInputStream = new FileInputStream(path);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            reader = new BufferedReader(inputStreamReader);
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                laststr += tempString;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return laststr;
    }


    /**
     * @param filePath
     * @param fileName
     * @return
     */
    //将文件转换成Byte数组
    public static byte[] getBytesByFile(String filePath, String fileName) {
        String path = filePath + File.separator + fileName;
        File file = new File(path);
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            byte[] data = bos.toByteArray();
            bos.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}
