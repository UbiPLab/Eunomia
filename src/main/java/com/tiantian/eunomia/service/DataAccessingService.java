package com.tiantian.eunomia.service;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.model.Md;
import it.unisa.dia.gas.jpbc.Element;

/**
 * 数据访问
 *
 * @author tiantian152
 */
public interface DataAccessingService {

    // cpi 对案件 X 进行调查
    // 案件 X 希望从 cb 获得一些数据 di
    // cpi 根据调查要求构建元数据 mdj
    // mdj 如：（“out-vehicle”，“short video”，20200526、08：00）
    // cpi_j 构造一个范围证明
    // cpi_j 为匿名身份验证准备了类似的 计算 HVj = h2(pij || pi_j' || Cj || mdj || pi_j'')
    // 构造Tx4

    /**
     * 检查数据用户是否可以查看这些数据
     *
     * @param username 用户名
     * @param mdj      元数据 mdj
     * @return 验证结果
     */
    boolean verifyDataUser(String username, Md mdj);

    /**
     * 生成 Tx4 Json
     *
     * @param pij 身份凭证 pii
     * @param mdj 构造的元数据
     * @param waj 数据用户时间
     * @return
     */
    JSONObject generateTx4Json(Element pij, Md mdj, String waj);

    /**
     * 对 Tx4 的内容签名
     *
     * @param privateKeyBase64 从以太坊获得的私钥的 base64
     * @param cri              cr_i 用来在区块链端查找公钥来验证
     * @param ts
     * @param tx4Json          tx4Json
     * @return 签名后的 Tx4 以及 区块链检索私钥用的 cri
     */
    JSONObject generateTx4Ecdsa(String privateKeyBase64, Element cri, String ts, JSONObject tx4Json);
}
