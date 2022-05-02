package com.tiantian.eunomia;

import com.tiantian.eunomia.service.DataUserEntityRegistration;
import com.tiantian.eunomia.service.impl.DataUserEntityRegistrationImpl;
import com.tiantian.eunomia.utils.HashUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.NoSuchAlgorithmException;


@SpringBootTest
public class DataUserEntityRegistrationImplTest {

    private static final Pairing pairing = PairingFactory.getPairing("a1.properties");    //	生成pairing对象;

    @Autowired
    DataUserEntityRegistration dataUserEntityRegistration = new DataUserEntityRegistrationImpl();

    static String[] attrs = {"PID:0000000001", "MCC-MNC:525-05", "TS:1577867245"};

    @Test
    public void test01() {

        //	生成主密钥ski
        Element ski = dataUserEntityRegistration.generateSki("password");

        //  随机选择ri属于Zq
        Element ri = dataUserEntityRegistration.generateRi();

        //  计算伪身份pii
        Element pii = dataUserEntityRegistration.calculatePii(ri, ski);

        //  构造属性集
        Element[] e_attr = new Element[attrs.length];
        for (int i = 0; i < attrs.length; i++) {
            try {
                e_attr[i] = HashUtil.h1Zr(attrs[i]);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        Element rii = pairing.getZr().newRandomElement().getImmutable();

        //  计算凭证cri
        Element cri = dataUserEntityRegistration.calculateCri(rii, ski);

        //	零知识证明验证是否注册成功
        Element[] proof_cri = dataUserEntityRegistration.generateCriProof(cri, rii, ski, e_attr);
        Element[] proof_pii = dataUserEntityRegistration.generatePiiProof(pii, ri, ski);
        boolean result_cri = dataUserEntityRegistration.verifyCriProof(proof_cri, ri);
        boolean result_pii = dataUserEntityRegistration.verifyPiiProof(proof_pii);
        boolean result = result_cri & result_pii;
        if (result)
            System.out.println("数据提供者注册成功！！！");
        else
            System.out.println("零知识证明未通过，数据提供者注册失败！！！");
    }


}
