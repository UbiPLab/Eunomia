package com.tiantian.eunomia.service;

import com.alibaba.fastjson.JSONObject;

/**
 * @author shubham
 */
public interface DataAnalysisService {

    /**
     * 生成Tx7
     * @param H2Rj
     * @return
     */
    JSONObject generateTx7Json(String H2Rj);

    /**
     * 生成Tx8
     * @param X
     * @return
     */
    JSONObject generateTx8Json(String X);


}
