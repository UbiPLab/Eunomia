package com.tiantian.eunomia.controller;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.mapper.CasesMapper;
import com.tiantian.eunomia.mapper.DataUserMapper;
import com.tiantian.eunomia.mapper.dataUpload.EvidenceMapper;
import com.tiantian.eunomia.model.Md;
import com.tiantian.eunomia.model.TxByCri;
import com.tiantian.eunomia.model.dataUpload.Evidence;
import com.tiantian.eunomia.model.dataUser.Cases;
import com.tiantian.eunomia.model.dataUser.DataUser;
import com.tiantian.eunomia.service.DataAccessingService;
import com.tiantian.eunomia.service.impl.DataAccessingServiceImpl;
import com.tiantian.eunomia.utils.Base64Util;
import com.tiantian.eunomia.utils.HttpCallOtherInterfaceUtils;
import it.unisa.dia.gas.jpbc.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据访问 Controller
 *
 * @author tiantian152
 */
@Controller
public class DataAccessingController {

    @Autowired
    DataAccessingService dataAccessingService = new DataAccessingServiceImpl();

    @Autowired
    private DataUserMapper dataUserMapper;

    @Autowired
    EvidenceMapper evidenceMapper;

    @Autowired
    CasesMapper caseMapper;

    @RequestMapping("/createCase")
    public String createCasa(@RequestParam("caseName") String caseName,
                             @RequestParam("caseTime") String caseTime,
                             @RequestParam("casePlace") String casePlace,
                             @RequestParam("caseType") String caseType,
                             @RequestParam("dataType") String dataType,
                             Model model,
                             HttpServletRequest request){
        System.out.println("==================开始创建案件==================");
        String username = (String) request.getSession().getAttribute("username");
        System.out.println("username" + username);
        System.out.println("casePlace" + casePlace);
        System.out.println("caseType" + caseType);
        System.out.println("time" + caseTime);

        int caseId = 0;
        DateFormat  dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateTime = null;
        try {
            dateTime = dateFormat.parse(caseTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Cases aCase = new Cases(0,username,caseTime,casePlace,caseType,dataType,caseName,"");
        System.out.println(aCase.getTime());
        caseId = caseMapper.insert(aCase);
//        int Id = aCase.getCaseId();
//        String cId = Integer.toString(Id);
        model.addAttribute("msg", "创建成功");
        String url = "http://localhost:8091/toWeb3AddTx?showType=EunomiaCase&functionName=initEunomiaCase&criOrIndex=" + caseId + "&json=" + " ";

        return "redirect:" + url;


//        return "page-createCase";

    }

    @RequestMapping("/dataAccessing")
    public String dataAccessing(@RequestParam("username") String username,
                                @RequestParam("caseId") String caseId,
                                @RequestParam("caseTime") String caseTime,
                                @RequestParam("casePlace") String casePlace,
                                @RequestParam("caseType") String caseType,
                                @RequestParam("dataType") String dataType,
                                Model model,
                                HttpServletRequest request) {
        System.out.println("==================数据用户数据访问开始==================");

//        String username = "井岗派出所交警";

        System.out.println("casetime"+ caseTime);
        System.out.println("datatype"+ dataType);
        DateFormat  dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateTime = null;
        try {
            dateTime = dateFormat.parse(caseTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        Date caseTime = new Date();
//        String casePlace = "稻香村街道";
//        String caseType = "交通事故";
//        String dataType = "video";
        Md mdj = new Md(dateTime, casePlace, caseType, dataType);

        if(username.isEmpty() != true){
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

                // 获取签名私钥
                System.out.println("==================getKeyPair Start==================");
                Map<String, Object> getPrivateKeyMap = new HashMap<>(1);
                getPrivateKeyMap.put("cri", Base64Util.elementToBase64(cri));
                String ts = Long.toString(System.currentTimeMillis());
                getPrivateKeyMap.put("ts", ts);
                JSONObject testJson = new JSONObject(getPrivateKeyMap);
                String getKeyPairResult = HttpCallOtherInterfaceUtils.doPost("getKeyPair", testJson, request);
                System.out.println("返回参数：" + getKeyPairResult);
                System.out.println("==================getKeyPair Finish==================");



//            String privateKeyBase64 = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCBHAiqa9Nf7zdFWUXz3zzUztGWywYMPS9al8R4mt2Y8aQ==";
                JSONObject tx4EcdsaByCriJson = dataAccessingService.generateTx4Ecdsa(getKeyPairResult, cri, ts, tx4Json);
                String txEcdsaBase64 = (String) tx4EcdsaByCriJson.get("txEcdsaBase64");
                System.out.println("tx4EcdsaByCriJson=" + tx4EcdsaByCriJson);

                // 生成要发给区块链的完整json
                JSONObject tx4 = (JSONObject) JSONObject.toJSON(new TxByCri(tx4Json.toJSONString(), tx4EcdsaByCriJson.toJSONString()));
                System.out.println("tx4=" + tx4);

                String sr = HttpCallOtherInterfaceUtils.doPost("verifyTx4", tx4, request);
                System.out.println("返回参数：" + sr);
                getViewByLAndA(username,caseId,casePlace,caseType,model);
                String username1 = null;
                String casePlace1 = null;
                String caseType1 = null;
                String caseId1 = null;
                try {
                    username1 = URLEncoder.encode(username,"utf8");
                    casePlace1 = URLEncoder.encode(casePlace,"utf8");
                    caseType1 = URLEncoder.encode(caseType,"utf8");
                    caseId1 = URLEncoder.encode(caseId,"utf8");

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }



                System.out.println("==================数据用户数据访问结束==================");
                String url = "http://localhost:8091/toWeb3AddTx?showType=EunomiaCase&functionName=submit&criOrIndex=" + caseId + "&json=" + txEcdsaBase64
                        +"&username=" + username1 + "&caseId=" + caseId1 + "&casePlace=" + casePlace1 + "&caseType=" + caseType1;
                return "redirect:" + url;
//            return "page-invoices";
            } else {
                System.out.println("您没有权限访问");
                model.addAttribute("msg", "您没有权限访问");
                String username1 = (String) request.getSession().getAttribute("username");
                List<Cases> cases = caseMapper.getCase();
                model.addAttribute("case",cases);
                model.addAttribute("name",username1);
                return "page-caseList";
            }
        }else {
            System.out.println("请重新登录");
            model.addAttribute("msg", "请重新登录");
            return "page-caseList";
        }

    }

    @RequestMapping("/evidence")
    public String getViewByLAndA(@RequestParam("username") String username,
                                 @RequestParam("caseId") String caseId,
                                 @RequestParam("casePlace") String casePlace,
                                 @RequestParam("caseType") String caseType,Model model){
        List<Evidence> view = evidenceMapper.getEvidenceByLAndA(casePlace,caseType);
        model.addAttribute("Id",caseId);
        model.addAttribute("evd",view);
        model.addAttribute("name",username);
        return "page-invoices";
    }

    @RequestMapping("/case")
    public String getCaseList(HttpServletRequest request,Model model){
        String username = (String) request.getSession().getAttribute("username");
        List<Cases> cases = caseMapper.getCase();
        model.addAttribute("case",cases);
        model.addAttribute("name",username);
        return "page-caseList";
    }

    @RequestMapping("/failure")
    public String getCaseListFailure(HttpServletRequest request,Model model){
        String username = (String) request.getSession().getAttribute("username");
        List<Cases> cases = caseMapper.getCase();
        model.addAttribute("case",cases);
        model.addAttribute("name",username);
        model.addAttribute("msg","检索失败");
        return "page-caseList";
    }


}
