package com.tiantian.eunomia.service;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.model.Aci;
import com.tiantian.eunomia.model.PkMsk.PkMsk;

import com.tiantian.eunomia.model.TxEcdsa;
import it.unisa.dia.gas.jpbc.Element;

import java.math.BigInteger;

public interface DataProviderEntityRegistration {

    /**
     * 生成Tx1
     *
     * @param au  身份
     * @param aci 匿名凭证
     * @return TX1json
     */
    public JSONObject generateTX1Json(String au, Aci aci);

    /**
     * 生成TX1
     */
    public TxEcdsa generateTx1(String au, Aci aci);

    /**
     * 保存Cri和用户名到本地
     *
     * @param cri
     * @param username
     * @return
     */
    public boolean saveCri(String cri, String username);

    /**
     * 读取保存的用户名和Cri
     *
     * @param username
     * @return
     */
    public String readCri(String username);

    /**
     * 生成Ai
     *
     * @param cri_array
     * @param N
     * @return
     */
    public BigInteger calculateAi(Element[] cri_array, BigInteger N);

    /**
     * \
     * 生成wi
     *
     * @param cri_array
     * @param crj
     * @param N
     * @return
     */
    public BigInteger calculateWi(Element[] cri_array, Element crj, BigInteger N);

    /**
     * 验证Ai' = wi^cri
     *
     * @param Ai
     * @param wi
     * @param cri
     * @param N
     * @return
     */
    public boolean verifyAi(BigInteger Ai, BigInteger wi, Element cri, BigInteger N);

    /**
     * 存ski
     *
     * @param ski
     * @param username
     * @return
     */
    public boolean  saveSki(Element ski, String username);

    /**
     * 读ski
     *
     * @param username
     * @return
     */
    public Element readSki(String username);
}
