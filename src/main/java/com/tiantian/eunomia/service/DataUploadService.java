package com.tiantian.eunomia.service;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.model.Ed.Ed;
import com.tiantian.eunomia.model.Ed.EdBase64;
import com.tiantian.eunomia.model.Ed.EdBase64Json;
import com.tiantian.eunomia.model.pk.Pk;
import it.unisa.dia.gas.jpbc.Element;

import java.util.List;

/**
 * @author shubham
 */
public interface DataUploadService {

    /**
     * 生成edi
     *
     * @param attrs 属性集
     * @param pk    公钥
     */
    void genCt(String[] attrs, Pk pk);

    /**
     * 获取edi
     *
     * @param attrs 属性集
     * @param pk    公钥
     * @return edi
     */
    Ed uploading(String[] attrs, Pk pk);

    /**
     * Ed转json
     *
     * @param edi 加密数据
     * @return edijson格式
     */
    String transform(Ed edi);

    /**
     * json转base64
     * @param EdJson
     * @return
     */
    EdBase64 jsonTransform(String EdJson);

    /**
     * Ed转Base64
     * @param edi
     * @return
     */
    EdBase64 transformBase64(Ed edi);


    /**
     * base64转Ed
     * @param edBase64
     * @return
     */
    Ed getEd(EdBase64 edBase64);

    /**
     * 保存Ed
     * @param edBase64
     * @return
     */
    boolean saveEd(EdBase64 edBase64,String tel);

    /**
     * 读取Ed
     * @param tel
     * @return
     */
    EdBase64 readEd(String tel);

    /**
     * 生成Hv1
     *
     * @param pii 零知识证明中的pii
     * @param pIi πi
     * @param Ci  一组cri
     * @param mdi 属性固定数组
     * @return HV1
     */
    String calculateHv1(Element pii, Element[] pIi, Element[] Ci, String[] mdi);

    /**
     * 生成Hv2
     *
     * @param Edi 加密数据
     * @return Hv2
     */
    String calculateHv2(Element[] Edi);

    /**
     * 生成Tx3中内容
     *
     * @param pii 零知识证明中的pii
     * @param pi  注册生成的
     * @param Ci  一组cri
     * @param mdi 属性固定数组
     * @param HV1 HV1
     * @param HV2 HV2
     * @return Tx3json
     */
    JSONObject generateTx3Json(Element pii, Element[] pi, Element[] Ci, String[] mdi, String HV1, String HV2);

    /**
     * 对 Tx3 的内容签名
     *
     * @param privateKeyBase64 从以太坊获得的私钥的 base64
     * @param cri              cr_i 用来在区块链端查找公钥来验证
     * @param ts
     * @param tx3Json          tx3Json
     * @return 签名后的 Tx3 以及 区块链检索私钥用的 cri
     */
    JSONObject generateTx3Ecdsa(String privateKeyBase64, Element cri, String ts,JSONObject tx3Json);

    /**
     * @return 列表
     */
    List<EdBase64Json> getEdBase64Json();

    /**
     * @param tel
     * @return edbase64json对象
     */
    EdBase64Json selectEdByTel(String tel);

    /**
     * @param edBase64Json
     * @return 插入edvbase64json对象
     */
    void insertEd(EdBase64Json edBase64Json);
//    public TxEcdsaByCri generateTx4Ecdsa (String privateKeyBase64,);
}
