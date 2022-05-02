package com.tiantian.eunomia.service;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.model.Ed.Ed;
import com.tiantian.eunomia.model.MasterKey.MasterKey;
import com.tiantian.eunomia.model.TxEcdsa;
import com.tiantian.eunomia.model.msk.Msk;
import it.unisa.dia.gas.jpbc.Element;

import java.io.IOException;

/**
 * 数据检索
 *
 * @author tiantian152
 */
public interface DataRetrievalService {

    /**
     * 生成g1^r
     *
     * @param r   r
     * @param msk msk
     * @return g1^r
     */
    Element generateG1PowR(Element r, Msk msk);

    /**
     * 计算prk的位分解
     *
     * @param prk prk
     * @return 位分解结果
     */
    int[] generatePrkBi(Element prk);

    /**
     * 产生一组随机值ri
     *
     * @param len 长度
     * @return 一组随机值ri
     */
    Element[] generateRandomRi(int len);

    /**
     * 计算 Ci 和 R
     *
     *
     * @param b     b
     * @param arrRi arr_ri
     * @param gr    gr
     * @param msk   msk
     * @return Ci 和 R
     */
    Element[][] generateCommitment(int[] b, Element[] arrRi, Element gr, Msk msk);

    /**
     * 计算 C
     *
     * @param ciR ciR
     * @return C
     */
    Element generateC(Element[][] ciR);

    /**
     * 验证 C = g1^r * puk^r
     *
     * @param C   C
     * @param R   R
     * @param puk puK
     * @param msk msk
     * @return C
     */
    boolean verifyC(Element C, Element R, Element puk, Msk msk);

    /**
     * 生成 kij
     *
     * @param i   i
     * @param j   j
     * @param Ci  Ci
     * @param msk msk
     * @return kij
     */
    String generateKij(int i, int j, Element Ci, Msk msk);

    /**
     * 计算 pi
     *
     * @param i   i
     * @param Ci  Ci
     * @param msk msk
     * @return pi
     */
    String generatePi(int i, Element Ci, Msk msk);

    /**
     * 生成 CiProof
     *
     * @param gr  gr
     * @param ri  ri
     * @param bi  bi
     * @param msk msk
     * @return Ci
     */
    Element[] generateCiProof(Element gr, Element ri, int bi, Msk msk);

    /**
     * 验证 CiProof
     *
     * @param gr    gr
     * @param proof proof
     * @param msk   msk
     * @return 验证结果
     */
    boolean verifyCiProof(Element gr, Element[] proof, Msk msk);

    /**
     * CPD计算Pi，发送给 cpij
     *
     * @param Ci  Ci
     * @param r   r
     * @param msk msk
     * @return Pi
     */
    String[][] generatePi(Element[] Ci, Element r, Msk msk);

    /**
     * @param Ci
     * @param r
     * @param msk
     * @return
     */
    String[][] generateKi(Element[] Ci, Element r, Msk msk);

    /**
     * cpij计算P'i,发送给CPD
     *
     * @param gr gr
     * @param Ri Ri
     * @param Pi Pi
     * @param b  b
     * @return PPi
     */
    String[] generatePpi(Element gr, Element[] Ri, String[] Pi, int[] b);

    /**
     * @param gr
     * @param Ri
     * @param Pi
     * @param b
     * @return ki
     */
    String[] generateKi(Element gr, Element[] Ri, String[] Pi, int[] b);

    /**
     * CPD验证
     *
     * @param ppi PPi
     * @param ki0 ki0
     * @return 验证结果
     */
    boolean verifyPpi(String[] ppi, String[] ki0);

    /**
     * 计算Edi' 加密
     *
     * @param di      di
     * @param message massage
     * @return aesBuf 加密后的字节数租
     */
    byte[] encrypt(Element di, String message);

    /**
     * 解密
     *
     * @param key    key
     * @param edi    edi
     * @param aesBuf aesBuf
     * @return 解密后的字节数租
     */
    byte[] decrypt(MasterKey key, Ed edi, byte[] aesBuf);

    /**
     * 还没有看这是干啥的
     *
     * @param attrs   attrs
     * @param message massage
     * @param msk     msk
     */
    void retrieval(String[] attrs, String message, Msk msk);

    /**
     * 生成Tx5
     * @param au
     * @param pi
     * @return
     */
    JSONObject generateTx5Json(String au,Element[] pi);

    /**
     *
     *生成签名后的Tx5
     * @param tx5Json
     * @return
     */
    TxEcdsa generateTx5Ecdsa(JSONObject tx5Json);

    /**
     * 生成Tx6
     * @param cpd
     * @param pi
     * @return
     */
    JSONObject generateTx6Json(String cpd,Element[] pi);

    /**
     * 生成签名后的Tx6
     * @param tx6Json
     * @return
     */
    TxEcdsa generateTx6Ecdsa(JSONObject tx6Json);

    /**
     * @return r
     */
    Element getR();


    /**
     * @param path
     * @param ki0
     * @param ki1
     * @return
     */
    String[][] encWaterMask(String path,String[] ki0,String[] ki1);

    /**
     * @param path 图片加密路径
     * @param key 对称秘钥
     */
    String decWaterMask(String[][] path,String[] key) throws IOException;

    /**
     * 保存Ki数组
     * @param Ki0
     * @param evidenceId
     * @return
     */
    boolean saveKi0(String[] Ki0,String evidenceId);

    /**
     * @param Ki1
     * @param evidenceId
     * @return
     */
    boolean saveKi1(String[] Ki1,String evidenceId);

    /**
     * 读取Ki数组
     * @param evidenceId
     * @return
     */
    String[] readKi0(String evidenceId);

    /**
     * @param evidenceId
     * @return
     */
    String[] readKi1(String evidenceId);
}
