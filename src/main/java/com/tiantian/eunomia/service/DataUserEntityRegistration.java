package com.tiantian.eunomia.service;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.model.*;
import com.tiantian.eunomia.model.MasterKey.MasterKey;
import com.tiantian.eunomia.model.PkMsk.PkMsk;
import com.tiantian.eunomia.model.msk.Msk;
import it.unisa.dia.gas.jpbc.Element;

/**
 * @author tiantian152
 */
public interface DataUserEntityRegistration {

    /**
     * 产生ski作为主密钥
     *
     * @param password 密码
     * @return 主密钥
     */
    Element generateSki(String password);

    /**
     * 随机选择ri属于Zq
     *
     * @return 随机数
     */
    Element generateRi();

    /**
     * 计算伪身份pii
     *
     * @param ri  随机数
     * @param ski 主密钥
     * @return 伪身份
     */
    Element calculatePii(Element ri, Element ski);

    /**
     * 计算Cri
     *
     * @param rii rii
     * @param ski ski
     * @return cri
     */
    Element calculateCri(Element rii, Element ski);

    /**
     * 生成cri零知识证明凭证
     *
     * @param cri  用户凭证
     * @param rii  随机数
     * @param ski  主密钥
     * @param attr 属性集
     * @return cri
     */
    Element[] generateCriProof(Element cri, Element rii, Element ski, Element[] attr);

    /**
     * 零知识证明验证
     *
     * @param proof cri的凭证
     * @param cri   用户凭证
     * @return 验证结果
     */
    boolean verifyCriProof(Element[] proof, Element cri);

    /**
     * 生成pii零知识证明凭证
     *
     * @param pii 随机数
     * @param ri  随机数
     * @param ski 主密钥
     * @return pii_proof
     */
    Element[] generatePiiProof(Element pii, Element ri, Element ski);

    /**
     * 零知识证明验证
     *
     * @param proof pii的凭证
     * @return 验证结果
     */
    boolean verifyPiiProof(Element[] proof);

    /**
     * 计算skj1
     *
     * @param msk msk
     * @return skj1
     */
    Element[] generateSkj1(Msk msk);

    /**
     * 计算skj
     *
     * @param attr attr
     * @param msk  msk
     * @return skj
     */
    Element[] generateSkj(String attr, Msk msk);

    /**
     * 计算skjx
     *
     * @param attrs attrs
     * @param msk   msk
     */
    void generateMasterSkjx(String[] attrs, Msk msk);

    /**
     * 计算skj3
     *
     * @param msk msk
     * @return skj3
     */
    Element[] generateSkj3(Msk msk);

    /**
     * 生成Tx2
     *
     * @param au  身份
     * @param aci 匿名凭证
     * @return TX2json
     */
    JSONObject generateTx2Json(String au, Aci aci);

    /**
     * 生成TX2
     *
     * @param tx2Json tx2Json
     * @return TX2jsonEcdsa
     */
    TxEcdsa generateTx2Ecdsa(JSONObject tx2Json);

    /**
     * Two generators g1 and g2 are selected for G1 and G2, respectively. They choose a1、a2 属于Zp and b1、b2、b3属于Zp,
     *
     * @return pkMsk
     */
    PkMsk generatePkMsk();

    /**
     * 保存 PkMsk 到本地中
     *
     * @param pkMsk    pkMsk
     * @param username 用户名
     * @return 是否保存成功
     */
    boolean savePkMsk(PkMsk pkMsk, String username);

    /**
     * 从本地读取 PkMsk
     *
     * @param username 用户名
     * @return PkMsk
     */
    PkMsk readPkMsk(String username);

    /**
     * 生成数据用户属性密钥 skey
     *
     * @param attrs 数据用户属性集
     * @param msk   数据用户msp
     * @return 属性密钥 skey
     */
    MasterKey generateSKey(String[] attrs, Msk msk);

    /**
     * 保存 sKey 到本地中
     *
     * @return 是否保存成功
     */
    boolean saveSKey(MasterKey sKey, String username);

    /**
     * 从本地读取 sKey
     *
     * @param username 用户名
     * @return sKey
     */
    MasterKey readSKey(String username);

    /**
     * @return H
     */
    Element[] selectHFromSystemInit();
}
