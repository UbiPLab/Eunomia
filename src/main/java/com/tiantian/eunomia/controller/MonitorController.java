package com.tiantian.eunomia.controller;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.mapper.*;
import com.tiantian.eunomia.mapper.dataProvider.DataProviderMapper;
import com.tiantian.eunomia.mapper.dataReport.ReportMapper;
import com.tiantian.eunomia.mapper.dataUpload.EvidenceMapper;
import com.tiantian.eunomia.model.dataUpload.Evidence;
import com.tiantian.eunomia.model.dataUser.*;
import com.tiantian.eunomia.utils.HttpCallOtherInterfaceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shubham
 */
@Controller
public class MonitorController {
    @Autowired
    private DataUserMapper dataUserMapper;

    @Autowired
    private DataUserInformationMapper dataUserInformationMapper;

    @Autowired
    private DataProviderMapper dataProviderMapper;

    @Autowired
    private CasesMapper casesMapper;

    @Autowired
    private EvidenceMapper evidenceMapper;

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private DataUserAttrsMapper dataUserAttrsMapper;

    @Autowired
    private StageMapper stageMapper;

    @RequestMapping("/toMonitor")
    public String Monitor1(Model model){
        int count1 = dataUserMapper.count();
        int count2 = dataProviderMapper.count();
        int count3 = casesMapper.count();
        int count4 = evidenceMapper.count();
        model.addAttribute("count1",count1);
        model.addAttribute("count2",count2);
        model.addAttribute("count3",count3);
        model.addAttribute("count4",count4);
        return "Monitor";
    }

    @RequestMapping("/welcome")
    public String welocme(Model model){
        int count1 = dataUserMapper.count();
        int count2 = dataProviderMapper.count();
        int count3 = casesMapper.count();
        int count4 = evidenceMapper.count();
        model.addAttribute("count1",count1);
        model.addAttribute("count2",count2);
        model.addAttribute("count3",count3);
        model.addAttribute("count4",count4);
        return "welcome";
    }

    @RequestMapping("/toUser")
    public String UserMonitor(Model model){
        List<DataUserAttrs> attrs = dataUserAttrsMapper.getAttrs();
        model.addAttribute("attrs",attrs);
        return "UserMonitor";
    }

    @RequestMapping("/toCase")
    public String CaseMonitor(Model model,HttpServletRequest request){
        List<Cases> cases = casesMapper.getCase();
        model.addAttribute("cases",cases);

        System.out.println("==================getStage Start==================");
        Map<String, Object> getStageMap = new HashMap<>(1);
        getStageMap.put("caseId", 1);
        JSONObject testJson1 = new JSONObject(getStageMap);
        String getStageResult = HttpCallOtherInterfaceUtils.doPost("getStage", testJson1, request);
        System.out.println("???????????????" + getStageResult);
        System.out.println("==================getStage End==================");

        return "CasesMonitor";
    }

    @RequestMapping("/toEvidence")
    public String EvidenceMonitor(Model model){
        List<Evidence> evidences = evidenceMapper.getEvidence();
        model.addAttribute("evidences",evidences);
        return "EvidenceMonitor";
    }

    @RequestMapping("/toBasicInformation")
    public String BasicInformation(String username,Model model){
        int userId = dataUserMapper.selectDataUserByUsername(username).get(0).getDataUserId();
        DataUserAttrs dataUserAttrs = dataUserAttrsMapper.getDataUser(userId);
        DataUserInformation dataUserInformation = dataUserInformationMapper.getDataUser(userId);
        model.addAttribute("attrs",dataUserAttrs);
        model.addAttribute("informs",dataUserInformation);
        return "BasicInformation";
    }

    @RequestMapping("/getStage1")
//    @ResponseBody
    public String getStage(String caseId,HttpServletRequest request,Model model){



        int ID = Integer.parseInt(caseId);
        //????????????
        System.out.println("==================getStage Start==================");
        Map<String, Object> getStageMap = new HashMap<>(1);
        getStageMap.put("caseId", ID);
        JSONObject testJson1 = new JSONObject(getStageMap);
        String getStageResult = HttpCallOtherInterfaceUtils.doPost("getStage", testJson1, request);
        System.out.println("???????????????" + getStageResult);
        if("WarrantRequest".equals(getStageResult)){
            getStageResult = "????????????";
        }else if ("Completed".equals(getStageResult)){
            getStageResult = "????????????";
        }else if ("DataRequest".equals(getStageResult)){
            getStageResult = "????????????";
        }else if ("DataRetrieval".equals(getStageResult)){
            getStageResult = "????????????";
        }else if ("DataAnalysis".equals(getStageResult)){
            getStageResult = "????????????";
        }else if ("ResultReport".equals(getStageResult)){
            getStageResult = "????????????";
        }else if ("InvertigationClosure".equals(getStageResult)){
            getStageResult = "????????????";
        }else if ("toStart".equals(getStageResult)){
            getStageResult = "????????????";
        }
        System.out.println("==================getStage End==================");

        casesMapper.updateStage(getStageResult,ID);

        List<Cases> cases = casesMapper.getCase();
        model.addAttribute("cases",cases);

        return "CasesMonitor";
    }

    @RequestMapping("/getStage2")
//    @ResponseBody
    public String getStage2(String caseId,HttpServletRequest request,Model model){



        int ID = Integer.parseInt(caseId);
        //????????????
        System.out.println("==================getStage Start==================");
        Map<String, Object> getStageMap = new HashMap<>(1);
        getStageMap.put("caseId", ID);
        JSONObject testJson1 = new JSONObject(getStageMap);
        String getStageResult = HttpCallOtherInterfaceUtils.doPost("getStage", testJson1, request);
        System.out.println("???????????????" + getStageResult);
        if("WarrantRequest".equals(getStageResult)){
            getStageResult = "????????????";
        }else if ("Completed".equals(getStageResult)){
            getStageResult = "????????????";
        }else if ("DataRequest".equals(getStageResult)){
            getStageResult = "????????????";
        }else if ("DataRetrieval".equals(getStageResult)){
            getStageResult = "????????????";
        }else if ("DataAnalysis".equals(getStageResult)){
            getStageResult = "????????????";
        }else if ("ResultReport".equals(getStageResult)){
            getStageResult = "????????????";
        }else if ("InvertigationClosure".equals(getStageResult)){
            getStageResult = "????????????";
        }else if ("toStart".equals(getStageResult)){
            getStageResult = "????????????";
        }
        System.out.println("==================getStage End==================");

        casesMapper.updateStage(getStageResult,ID);

        List<Cases> cases = casesMapper.getCase();
        model.addAttribute("case",cases);
        model.addAttribute("stage",getStageResult);

        return "page-caseList";
    }

    @RequestMapping("/getStage")
    @ResponseBody
    public String getStage(HttpServletRequest request,Model model){
        System.out.println("==================getStage Start==================");
        Map<String, Object> getStageMap = new HashMap<>(1);
        JSONObject testJson1 = new JSONObject(getStageMap);
        String getStageResult = HttpCallOtherInterfaceUtils.doPost("getStageList", testJson1, request);
        System.out.println("???????????????" + getStageResult);
        return getStageResult;
    }
}
