package com.tiantian.eunomia.controller;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.mapper.DataUserMapper;
import com.tiantian.eunomia.mapper.dataReport.ReportMapper;
import com.tiantian.eunomia.model.Tx;
import com.tiantian.eunomia.model.TxByCri;
import com.tiantian.eunomia.model.TxEcdsa;
import com.tiantian.eunomia.model.dataReport.Report;
import com.tiantian.eunomia.model.dataUser.DataUser;
import com.tiantian.eunomia.service.DataReportService;
import com.tiantian.eunomia.service.impl.DataReportServiceImpl;
import com.tiantian.eunomia.utils.Base64Util;
import com.tiantian.eunomia.utils.HashUtil;
import com.tiantian.eunomia.utils.HttpCallOtherInterfaceUtils;
import it.unisa.dia.gas.jpbc.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shubham
 */
@Controller
public class DataReportController {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private DataUserMapper dataUserMapper;

    private DataReportService dataReportService = new DataReportServiceImpl();

    @RequestMapping("/report")
    public String userReport(@RequestParam("caseId") String caseId,
                             @RequestParam("username") String username,
                             @RequestParam("report") String report,
                             Model model, HttpSession session, HttpServletRequest request) throws Exception {
        int caseID = Integer.parseInt(caseId);
//        int evidenceID = Integer.parseInt(evidenceId);

        int evidenceID = 1;
//        String username = (String) request.getSession().getAttribute("username");
        System.out.println(username);

        int reportId = 0;
        Report report1 = new Report(0,caseID,evidenceID,username,report);
        reportId = reportMapper.insert(report1);

        String H2Rj = HashUtil.h2(report);

        Map<String, Object> map = new HashMap<>(1);
        map.put("username", username);
        DataUser dataUser = dataUserMapper.selectByMap(map).get(0);
        System.out.println("dataUser=" + dataUser);

        Element cri = Base64Util.base64ToElement(dataUser.getCri());

        //??????TX7json
        JSONObject tx7Json = dataReportService.generateTx7Json(H2Rj);
        System.out.println("??????TX7json:" + tx7Json);

        //??????TX7
        TxEcdsa tx7Ecdsa = dataReportService.generateTx7Ecdsa(tx7Json);

        //??????TxEcdsa??????Json
        JSONObject tx7EcdsaJson = (JSONObject) JSONObject.toJSON(tx7Ecdsa);
        System.out.println("tx7EcdsaJson=" + tx7EcdsaJson);

        // ???????????????Json
        Tx tx = new Tx(tx7Json.toJSONString(), tx7EcdsaJson.toJSONString());
        JSONObject txJson = (JSONObject) JSONObject.toJSON(tx);
        System.out.println("txJson=" + txJson);

        // ??????????????????
        String sr = HttpCallOtherInterfaceUtils.doPost("verifyTx", txJson, request);
        System.out.println("???????????????" + sr);


        model.addAttribute("msg","????????????");

        String url = "http://localhost:8091/toWeb3AddTx?showType=EunomiaCase&functionName=report&criOrIndex=" + caseId + "&json=" + tx7Ecdsa.getTxEcdsaBase64()
                + "&caseId=" + caseId;

        return "redirect:" + url;

        //        return "forms-validation";
    }

    @RequestMapping("/close")
    public String Close(@RequestParam("caseId") String caseId,
                        Model model,HttpServletRequest request){

        String X = "close";
        //??????TX8json
        JSONObject tx8Json = dataReportService.generateTx8Json(X);
        System.out.println("??????TX8json:" + tx8Json);

        //??????TX8
        TxEcdsa tx8Ecdsa = dataReportService.generateTx8Ecdsa(tx8Json);

        //??????TxEcdsa??????Json
        JSONObject tx8EcdsaJson = (JSONObject) JSONObject.toJSON(tx8Ecdsa);
        System.out.println("tx8EcdsaJson=" + tx8EcdsaJson);

        // ???????????????Json
        Tx tx = new Tx(tx8Json.toJSONString(), tx8EcdsaJson.toJSONString());
        JSONObject txJson = (JSONObject) JSONObject.toJSON(tx);
        System.out.println("txJson=" + txJson);

        // ??????????????????
        String sr = HttpCallOtherInterfaceUtils.doPost("verifyTx", txJson, request);
        System.out.println("???????????????" + sr);

        String url = "http://localhost:8091/toWeb3AddTx?showType=EunomiaCase&functionName=close&criOrIndex=" + caseId + "&json=" + tx8Ecdsa.getTxEcdsaBase64();

        return "redirect:" + url;
    }

    @RequestMapping("/confirm")
    public String confirm(@RequestParam("caseId") String caseId,
                          Model model){
        model.addAttribute("caseId",caseId);
        return "page-confirm";
    }
}
