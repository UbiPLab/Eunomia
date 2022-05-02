package com.tiantian.eunomia.service.impl;

import com.tiantian.eunomia.mapper.HMapper;
import com.tiantian.eunomia.mapper.SystemInitMapper;
import com.tiantian.eunomia.model.H;
import com.tiantian.eunomia.model.SystemInit;
import com.tiantian.eunomia.service.SystemInitService;
import com.tiantian.eunomia.utils.Base64Util;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Random;

/**
 * @author 72451
 */
@Service
@Component
public class SystemInitServiceImpl implements SystemInitService {

    private static BigInteger q2, p2, N;
    private static Element[] h;

    private static final Pairing PAIRING = PairingFactory.getPairing("a1.properties");

    @Autowired
    private HMapper hMapper;

    @Autowired
    private SystemInitMapper systemInitMapper;


    @Override
    public void systemInit() {
        // 生成参数
        // 生成N
        N = generatePrime();
        // 生成p2、q2
        BigInteger[] p2q2Array = generateP2q2();
        // h1 ... hn
        h = generateH(5);

        // 转化对象为Base64
        // 获取N
        String NBase64 = Base64Util.bigIntegerToBase64(N);
        // 获取h
        String[] hBase64Array = Base64Util.elementArrayToBase64Array(h);

        // 数据持久化
        // 插入h
        H objectH = new H(0, hBase64Array[0], hBase64Array[1], hBase64Array[2], hBase64Array[3], hBase64Array[4]);
        int hResult = insertH(objectH);
        System.out.println("成功插入h表");
        // 插入SystemInit
        SystemInit systemInit = new SystemInit(0, NBase64, hResult);
        systemInitMapper.insert(systemInit);
        System.out.println("成功插入SystemInit表");
    }

    @Override
    public BigInteger generatePrime() {
        //	定义要产生的随机素数的长度，此处暂定为128bits
        int bitLen = 128;
        //	产生大素数p
        BigInteger p = BigInteger.probablePrime(bitLen, new Random());
        //产生大素数q
        BigInteger q = BigInteger.probablePrime(bitLen, new Random());
        //	计算p和q的乘积N
        N = p.multiply(q);
        return N;
    }

    @Override
    public BigInteger[] generateP2q2() {
        //定义要产生的随机素数的长度，此处暂定为128bits
        int bitLen = 128;
        boolean findFlag = false;

        while (!findFlag) {
            //控制循环的次数
            int r = 1;
            Random random = new Random();
            //	产生大素数q2
            q2 = BigInteger.probablePrime(bitLen, random);
            BigInteger b = BigInteger.valueOf(1);
            //	定义一个区间范围，当r超过10的时候，就再寻找一个新的q2，再算一次。这样可以加快寻找到p2的速度
            while (r <= 10) {
                long l1 = (long) Math.pow(2, r);
                BigInteger temp = BigInteger.valueOf(l1);
                //	判断q2*2^r + 1是否是素数
                if ((((q2.multiply(temp)).add(b)).isProbablePrime(1))) {
                    findFlag = true;
                    //	找到了满足条件的素数之后，赋值给p2
                    p2 = q2.multiply(temp).add(b);
                    break;
                }
                r++;
            }
        }
        BigInteger[] bigIntegers = new BigInteger[2];
        bigIntegers[0] = p2;
        bigIntegers[1] = q2;
        return bigIntegers;
    }

    @Override
    public Element[] generateH(int n) {
        h = new Element[n];
        for (int i = 0; i < n; i++) {
            //	生成生成元
            h[i] = PAIRING.getG1().newRandomElement().getImmutable();
        }
        return h;
    }

    @Override
    public int insertH(H h) {
        hMapper.insert(h);
        return h.getHId();
    }

}
