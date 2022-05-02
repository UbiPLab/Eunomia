package com.tiantian.eunomia.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.mapper.DataUserAttrsMapper;
import com.tiantian.eunomia.mapper.attrs.PoliceStationMapper;
import com.tiantian.eunomia.mapper.attrs.StreetMapper;
import com.tiantian.eunomia.mapper.view.DataUserAttrsViewMapper;
import com.tiantian.eunomia.model.Md;
import com.tiantian.eunomia.model.TxEcdsaByCri;
import com.tiantian.eunomia.model.attrs.Street;
import com.tiantian.eunomia.model.view.DataUserAttrsView;
import com.tiantian.eunomia.service.DataAccessingService;
import com.tiantian.eunomia.utils.Base64Util;
import com.tiantian.eunomia.utils.EcdsaUtils;
import com.tiantian.eunomia.utils.HashUtil;
import it.unisa.dia.gas.jpbc.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.util.*;

/**
 * 数据访问
 *
 * @author tiantian152
 */
@Service
@Component
public class DataAccessingServiceImpl implements DataAccessingService {

    @Autowired
    DataUserAttrsViewMapper dataUserAttrsViewMapper;

    @Autowired
    StreetMapper streetMapper;

    @Autowired
    PoliceStationMapper policeStationMapper;

    @Autowired
    DataUserAttrsMapper dataUserAttrsMapper;

    /**
     * 警察类型
     */
    private static final String[] POLICE_TYPE = {"交警", "刑警"};

    /**
     * 案件类型
     */
    private static final String[] CASE_TYPE = {"交通事故", "犯罪"};

    /**
     * 计算Tx4使用
     */
    public static EcdsaUtils ecdsa = new EcdsaUtils();

    @Override
    public boolean verifyDataUser(String username, Md mdj) {
        Map<String, Object> attrsViewMap = new HashMap<>(1);
        attrsViewMap.put("username", username);
        DataUserAttrsView dataUserAttrsView = dataUserAttrsViewMapper.selectByMap(attrsViewMap).get(0);

        // 案件发生时间：与警号对应，目前所有的警察都可以处理任意案件
        boolean resultTime;
        Map<String, Object> timeMap = new HashMap<>(1);
        timeMap.put("data_user_id",dataUserAttrsView.getDataUserId());
        Date startTime = dataUserAttrsMapper.selectByMap(timeMap).get(0).getPoliceStartTime();
        Date endTime = dataUserAttrsMapper.selectByMap(timeMap).get(0).getPoliceEndTime();
        Date time = mdj.getCaseTime();
        if(time.compareTo(startTime) != -1 && time.compareTo(endTime) != 1){
            resultTime = true;
        }else {
            resultTime = false;
        }


        // 案件发生地点：与警察局对应
        boolean resultPlace = false;
        String policeStation = dataUserAttrsView.getPoliceStation();
        Map<String, Object> policeStationMap = new HashMap<>(1);
        policeStationMap.put("police_station_name", policeStation);
        Integer policeStationId = policeStationMapper.selectByMap(policeStationMap).get(0).getPoliceStationId();

        Map<String, Object> streetMap = new HashMap<>(1);
        streetMap.put("street_name", mdj.getCasePlace());
        List<Street> streetList = streetMapper.selectByMap(streetMap);
        for (Street street : streetList) {
            if (street.getPoliceStationId().equals(policeStationId)) {
                resultPlace = true;
                break;
            }
        }

        // 案件种类(车祸、犯罪)：与警察类型(民警、交警)对应
        boolean resultType = false;
        if (POLICE_TYPE[0].equals(dataUserAttrsView.getPoliceType()) && CASE_TYPE[0].equals(mdj.getCaseType())) {
            // 如果是交警，是交通事故
            resultType = true;
        } else if (POLICE_TYPE[1].equals(dataUserAttrsView.getPoliceType()) && CASE_TYPE[1].equals(mdj.getCaseType())) {
            // 如果是刑警，是犯罪
            resultType = true;
        }

        // 数据类型：由警察输入，目前都可以处理
        boolean resultDataType = true;

        return resultTime && resultPlace && resultType && resultDataType;
    }

    @Override
    public JSONObject generateTx4Json(Element pij, Md mdj, String waj) {

        long ts = System.currentTimeMillis();
        Map<String, Object> map = new HashMap<>();
        map.put("operating", "Access");
        map.put("pij", Base64Util.elementToBase64(pij));
        map.put("mdj", mdj);
        map.put("waj", waj);
        String hvj = HashUtil.h2(pij.toString() + mdj.toString() + waj);
        map.put("hvj", hvj);
        map.put("ts", Long.toString(ts));
        return new JSONObject(map);
    }

    @Override
    public JSONObject generateTx4Ecdsa(String privateKeyBase64, Element cri, String ts, JSONObject tx4Json) {
        TxEcdsaByCri tx4EcdsaByCri = null;
        try {
            System.out.println("签名私钥=" + privateKeyBase64);
            byte[] privateKey = DatatypeConverter.parseBase64Binary(privateKeyBase64);
            byte[] txEcdsaByteArray = ecdsa.jdkEcdsa1(privateKey, tx4Json.toJSONString());
            System.out.println("签名后的json_byte数租=" + Arrays.toString(txEcdsaByteArray));
            tx4EcdsaByCri = new TxEcdsaByCri(Base64Util.elementToBase64(cri), ts, DatatypeConverter.printBase64Binary(txEcdsaByteArray));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 生成Json
        return (JSONObject) JSONObject.toJSON(tx4EcdsaByCri);
    }
}
