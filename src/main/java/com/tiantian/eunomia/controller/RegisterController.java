package com.tiantian.eunomia.controller;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.mapper.DataUserAttrsMapper;
import com.tiantian.eunomia.mapper.DataUserInformationMapper;
import com.tiantian.eunomia.mapper.DataUserMapper;
import com.tiantian.eunomia.model.Aci;
import com.tiantian.eunomia.model.MasterKey.MasterKey;
import com.tiantian.eunomia.model.PkMsk.PkMsk;
import com.tiantian.eunomia.model.Tx;
import com.tiantian.eunomia.model.TxEcdsa;
import com.tiantian.eunomia.model.dataUser.DataUserAttrs;
import com.tiantian.eunomia.model.dataUser.DataUserInformation;
import com.tiantian.eunomia.model.dataUser.DataUser;
import com.tiantian.eunomia.service.DataProviderEntityRegistration;
import com.tiantian.eunomia.service.DataUserEntityRegistration;
import com.tiantian.eunomia.service.DataUserService;
import com.tiantian.eunomia.service.impl.DataProviderEntityRegistrationImpl;
import com.tiantian.eunomia.service.impl.DataUserEntityRegistrationImpl;
import com.tiantian.eunomia.service.impl.DataUserServiceImpl;
import com.tiantian.eunomia.utils.Base64Util;
import com.tiantian.eunomia.utils.HttpCallOtherInterfaceUtils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tiantian
 */
@Controller
public class RegisterController {

    @Autowired
    DataUserMapper dataUserMapper;

    @Autowired
    DataUserService dataUserService = new DataUserServiceImpl();

    @Autowired
    DataUserInformationMapper dataUserInformationMapper;

    @Autowired
    DataUserAttrsMapper dataUserAttrsMapper;

    @Autowired
    DataProviderEntityRegistration dataProviderEntityRegistration = new DataProviderEntityRegistrationImpl();


    /**
     * ??????pairing??????
     */
    private static final Pairing PAIRING = PairingFactory.getPairing("a1.properties");

    @Autowired
    DataUserEntityRegistration dataUserEntityRegistration = new DataUserEntityRegistrationImpl();

    @RequestMapping("/dataUserRegister")
    public String register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           DataUserInformation dataUserInformation,
                           DataUserAttrs dataUserAttrs,
                           HttpServletRequest request,
                           Model model) {
        System.out.println("==================??????????????????==================");


        System.out.println(dataUserAttrs);

        //	???????????????ski
        Element ski = dataUserEntityRegistration.generateSki(password);
        System.out.println("???????????????ski=" + ski);
        boolean saveSkiResult = dataProviderEntityRegistration.saveSki(ski,username);


        //  ????????????ri??????Zq
        Element ri = dataUserEntityRegistration.generateRi();
        System.out.println("????????????ri??????Zq=" + ri);

        //  ???????????????pii
        Element pii = dataUserEntityRegistration.calculatePii(ri, ski);
        System.out.println("???????????????pii=" + pii);

        Element rii = PAIRING.getZr().newRandomElement().getImmutable();
        System.out.println("??????rii=" + rii);

        //  ????????????cri
        Element cri = dataUserEntityRegistration.calculateCri(rii, ski);
        System.out.println("????????????cri=" + cri);

        String criBase64 = Base64Util.elementToBase64(cri);
        String piiBase64 = Base64Util.elementToBase64(pii);
        String riBase64 = Base64Util.elementToBase64(ri);
        String riiBase64 = Base64Util.elementToBase64(rii);

        // ???????????????????????????
        List<DataUser> dataUserList = dataUserMapper.selectDataUserByUsername(username);
        System.out.println("username=" + username);
        if (dataUserList.isEmpty()) {
            System.out.println("??????????????????");
            // ????????????????????????????????????
            DataUser registerUser = new DataUser(0, username, criBase64, piiBase64, riBase64, riiBase64);


            // dp_i ??? cb ????????????????????????(Tx2)
            // ?????????????????????Tx2
            String[] attr = new String[2];
//            attr[0] = dataUserAttrs.getPoliceNumber();
            attr[0] = dataUserAttrs.getPoliceType();
            attr[1] = dataUserAttrs.getPoliceStation();
            Aci aci = new Aci(cri, attr, pii, ski);
            JSONObject tx2Json = dataUserEntityRegistration.generateTx2Json(dataUserAttrs.getPoliceStation(), aci);
            System.out.println("tx2Json=" + tx2Json);

            // ??????????????????Tx2,????????????
            TxEcdsa tx2Ecdsa = dataUserEntityRegistration.generateTx2Ecdsa(tx2Json);

            // ??????TxEcdsa??????Json
            JSONObject tx2EcdsaJson = (JSONObject) JSONObject.toJSON(tx2Ecdsa);
            System.out.println("tx2EcdsaJson=" + tx2EcdsaJson);

            // ???????????????Json
            Tx tx = new Tx(tx2Json.toJSONString(), tx2EcdsaJson.toJSONString());
            JSONObject txJson = (JSONObject) JSONObject.toJSON(tx);
            System.out.println("txJson=" + txJson);

//            // ??????????????????
            String sr = HttpCallOtherInterfaceUtils.doPost("verifyTx2", txJson, request);
            System.out.println("???????????????" + sr);

            if (Boolean.parseBoolean(sr)) {

                // ???????????????
                model.addAttribute("username", username);

                // ??????????????????
                dataUserMapper.insert(registerUser);

                // ?????? dataUserId
                int dataUserId = registerUser.getDataUserId();
                dataUserInformation.setDataUserId(dataUserId);
                int dataUserInformationResult = dataUserInformationMapper.insert(dataUserInformation);
                System.out.println("dataUserInformation???????????? = " + dataUserInformationResult);

                // ?????????????????????
                String startTime = "2021-07-01";
                String endTime = "2021-09-01";
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date timeStart = dateFormat.parse(startTime);
                    Date timeEnd = dateFormat.parse(endTime);
                    dataUserAttrs.setDataUserId(dataUserId);
                    dataUserAttrs.setPoliceStartTime(timeStart);
                    dataUserAttrs.setPoliceEndTime(timeEnd);
                    int dataUserAttrsResult = dataUserAttrsMapper.insert(dataUserAttrs);
                    System.out.println("dataUserAttrs???????????? = " + dataUserAttrsResult);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // ??????????????????????????????
                PkMsk pkMsk = dataUserEntityRegistration.generatePkMsk();
                boolean savePkMskResult = dataUserEntityRegistration.savePkMsk(pkMsk, username);
                MasterKey sKey = dataUserEntityRegistration.generateSKey(attr, pkMsk.getMsk());
                boolean result = dataUserEntityRegistration.saveSKey(sKey, username);
                if (savePkMskResult && result) {
                    System.out.println("????????????????????????");
                }
                System.out.println("==================??????????????????==================");
                String url = "http://localhost:8091/toWeb3AddTx?showType=cri&functionName=addDataUser&criOrIndex=" + Base64Util.elementToBase64(cri) + "&json=" + tx2Ecdsa.getTxEcdsaBase64();
                return "redirect:" + url;
            } else {
                model.addAttribute("msg", "??????????????????");
                System.out.println("==================??????????????????==================");
                // ?????????????????????????????????????????????
                model.addAttribute("dataUserInformationModel", dataUserInformation);
                model.addAttribute("dataUserAttrs", dataUserAttrs);

                return "page-register";
            }
        } else {
            model.addAttribute("msg", "???????????????????????????");
            // ?????????????????????????????????????????????
            model.addAttribute("dataUserInformationModel", dataUserInformation);
            model.addAttribute("dataUserAttrs", dataUserAttrs);
            System.out.println("==================??????????????????==================");
            return "page-register";
        }
    }

}
