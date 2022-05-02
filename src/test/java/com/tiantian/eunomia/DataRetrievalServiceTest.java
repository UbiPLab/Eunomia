package com.tiantian.eunomia;

import com.tiantian.eunomia.model.msk.Msk;
import com.tiantian.eunomia.service.DataRetrievalService;
import com.tiantian.eunomia.service.DataUserEntityRegistration;
import com.tiantian.eunomia.service.impl.DataRetrievalServiceImpl;
import com.tiantian.eunomia.service.impl.DataUserEntityRegistrationImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.ByteBuffer;
import java.util.ArrayList;

@SpringBootTest
public class DataRetrievalServiceTest {

    @Autowired
    DataRetrievalService dataRetrievalService = new DataRetrievalServiceImpl();

    @Autowired
    DataUserEntityRegistration dataUserEntityRegistration = new DataUserEntityRegistrationImpl();

    @Test
    public void test01() {
        String[] attrs = {"PID:0000000001", "MCC-MNC:525-05", "TS:1577867245"};
        String massage = "This is a test!";
        Msk msk = dataUserEntityRegistration.generatePkMsk().getMsk();
        dataRetrievalService.retrieval(attrs, massage, msk);
    }

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

    @Test
    public void stringTransByte(){
        String[] ki0 = new String[20];
        for(int i = 0;i<20;i++){
            ki0[i] = Integer.toString(i);
        }
        byte[] ki0Byte = serialize(ki0);
        System.out.println("ki0Byte=" + ki0Byte);

        String[] ki0String = unserialize(ki0Byte);
        System.out.println("ki0String" + ki0String);

    }

    @Test
    public void test1(){
        String url = "D:\\Idea Project\\xinan-competition\\source\\20210531233252_3.jpeg";
        String str = url.replaceAll("\\\\",",");
        System.out.println("str= " + str);
        str = str.replaceAll(",","\\\\\\\\");
        System.out.println("str= " + str);
    }
}
