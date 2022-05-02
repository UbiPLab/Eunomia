package com.tiantian.eunomia.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.service.DataAnalysisService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shubham
 */
public class DataAnalysisServiceImpl implements DataAnalysisService {
    @Override
    public JSONObject generateTx7Json(String H2Rj) {

        Date date = new Date();
        long ts = System.currentTimeMillis();

        Map<String,Object> map = new HashMap<>();
        map.put("operating","Report");
        map.put("H2Rj",H2Rj);
        map.put("data",date.toString());
        map.put("ts",Long.toString(ts));
        return new JSONObject(map);
    }

    @Override
    public JSONObject generateTx8Json(String X) {
        Date date = new Date();
        long ts = System.currentTimeMillis();

        Map<String,Object> map = new HashMap<>();
        map.put("operating","Close");
        map.put("X",X);
        map.put("data",date.toString());
        map.put("ts",Long.toString(ts));

        return new JSONObject(map);
    }
}
