package com.tiantian.eunomia.service;

import com.tiantian.eunomia.model.H;
import it.unisa.dia.gas.jpbc.Element;

import java.math.BigInteger;

/**
 * @author tiantian152
 */
public interface SystemInitService {

    /**
     * 系统初始化
     */
    void systemInit();

    /**
     * 随机生成两个素数p1 q1，并计算N=p1q1，函数最后返回N。
     *
     * @return N 两个素数的乘积
     */
    BigInteger generatePrime();

    /**
     * 对于r ≥ 1，生成满足 p2 = q2*2^r + 1 的素数 p2 和 q2。
     *
     * @return p2 q2
     */
    BigInteger[] generateP2q2();

    /**
     * 随机选择h0 h1 ... hn 是G的生成元，返回存储h的数组e。
     *
     * @param n h的个数
     * @return h
     */
    Element[] generateH(int n);

    /**
     * 插入h
     *
     * @param h h
     * @return 插入结果
     */
    int insertH(H h);

}
