package com.tiantian.eunomia.service;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.model.TxEcdsa;

public interface DataReportService {

    /**
     * 获取Tx7
     * @param H2Rj hash过后的report
     * @return
     */
    JSONObject generateTx7Json(String H2Rj);

    /**
     * 获取签名后的Tx7
     * @param tx7Json
     * @return
     */
    TxEcdsa generateTx7Ecdsa(JSONObject tx7Json);


    /**
     * 获取Tx8
     * @param X
     * @return
     */
    JSONObject generateTx8Json(String X);

    /**
     * 获取签名后的Tx8
     * @param tx8Json
     * @return
     */
    TxEcdsa generateTx8Ecdsa(JSONObject tx8Json);

}
