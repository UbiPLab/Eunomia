package com.tiantian.eunomia;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.mapper.DataUserMapper;
import com.tiantian.eunomia.model.Md;
import com.tiantian.eunomia.model.Tx;
import com.tiantian.eunomia.model.dataUser.DataUser;
import com.tiantian.eunomia.service.DataAccessingService;
import com.tiantian.eunomia.service.impl.DataAccessingServiceImpl;
import com.tiantian.eunomia.utils.Base64Util;
import it.unisa.dia.gas.jpbc.Element;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class DataAccessingServiceTest {

    @Autowired
    DataAccessingService dataAccessingService = new DataAccessingServiceImpl();

    @Autowired
    private DataUserMapper dataUserMapper;

    @Test
    public void verifyDataUserTest() {

        String username = "井岗派出所交警";

        Date caseTime = new Date();
        String casePlace = "稻香村街道";
        String caseType = "车祸";
        String dataType = "video";
        Md mdj = new Md(caseTime, casePlace, caseType, dataType);

        boolean result = dataAccessingService.verifyDataUser(username, mdj);
        System.out.println("验证结果=" + result);

        Map<String, Object> map = new HashMap<>(1);
        map.put("username", username);
        DataUser dataUser = dataUserMapper.selectByMap(map).get(0);
        System.out.println("dataUser=" + dataUser);


        if (result) {
            System.out.println("名为 “" + username + "” 的数据用户有权限进行该数据访问");
            // 如果验证通过，就要将Tx4提交到区块链
            // 获取需要的参数
            Element pij = Base64Util.base64ToElement(dataUser.getPii());
            Element cri = Base64Util.base64ToElement(dataUser.getCri());
            String waj = "[0,210531]";

            // 生成 tx4Json
            JSONObject tx4Json = dataAccessingService.generateTx4Json(pij, mdj, waj);
            System.out.println("tx4Json=" + tx4Json);

            // 签名
            // This is a test!
            String privateKeyBase64 = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCBHAiqa9Nf7zdFWUXz3zzUztGWywYMPS9al8R4mt2Y8aQ==";
            JSONObject tx4EcdsaByCriJson = dataAccessingService.generateTx4Ecdsa(privateKeyBase64, cri, null, tx4Json);
            System.out.println("tx4EcdsaByCriJson=" + tx4EcdsaByCriJson);

            // 生成要发给区块链的完整json
            JSONObject tx4 = (JSONObject) JSONObject.toJSON(new Tx(tx4Json.toJSONString(), tx4EcdsaByCriJson.toJSONString()));
            System.out.println("tx4=" + tx4);

//            String sr = HttpCallOtherInterfaceUtils.doPost("getTx4", tx4, request);
//            System.out.println("返回参数：" + sr);

        }
    }
}
