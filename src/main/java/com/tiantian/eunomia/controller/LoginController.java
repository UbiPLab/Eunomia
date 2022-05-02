package com.tiantian.eunomia.controller;

import com.tiantian.eunomia.mapper.DataUserAttrsMapper;
import com.tiantian.eunomia.mapper.DataUserMapper;
import com.tiantian.eunomia.model.dataUser.DataUserAttrs;
import com.tiantian.eunomia.model.dataUser.DataUser;
import com.tiantian.eunomia.service.DataUserEntityRegistration;
import com.tiantian.eunomia.service.impl.DataUserEntityRegistrationImpl;
import com.tiantian.eunomia.utils.Base64Util;
import com.tiantian.eunomia.utils.HashUtil;
import it.unisa.dia.gas.jpbc.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author tiantian152
 */
@Controller
public class LoginController {

    @Autowired
    private DataUserMapper dataUserMapper;

    @Autowired
    private DataUserAttrsMapper dataUserAttrsMapper;

    @Autowired
    DataUserEntityRegistration dataUserEntityRegistration = new DataUserEntityRegistrationImpl();

    @RequestMapping("/dataUserLogin")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session,
                        Model model) {
        System.out.println("==================数据用户登录开始==================");

        //	生成主密钥ski
        Element ski = dataUserEntityRegistration.generateSki(password);

        Map<String, Object> map = new HashMap<>(1);
        map.put("username", username);
        System.out.println(map);
        List<DataUser> dataUserList = dataUserMapper.selectByMap(map);
        System.out.println("userModelList=" + dataUserList);

        if (dataUserList != null) {
            // 如果用户存在
            // 获取值
            String criBase64 = dataUserList.get(0).getCri();
            String piiBase64 = dataUserList.get(0).getPii();
            String riBase64 = dataUserList.get(0).getRi();
            String riiBase64 = dataUserList.get(0).getRii();

            Element cri = Base64Util.base64ToElement(criBase64);
            Element pii = Base64Util.base64ToElement(piiBase64);
            Element ri = Base64Util.base64ToElementForZr(riBase64);
            Element rii = Base64Util.base64ToElementForZr(riiBase64);

            int dataUserId = dataUserList.get(0).getDataUserId();
            System.out.println("dataUserId=" + dataUserId);
            Map<String, Object> attrMap = new HashMap<>(1);
            attrMap.put("data_user_id", dataUserId);
            DataUserAttrs dataUserAttrs = dataUserAttrsMapper.selectByMap(attrMap).get(0);
            System.out.println("dataUserAttrs=" + dataUserAttrs);

            String[] attrsBase64 = new String[3];
            attrsBase64[0] = dataUserAttrs.getPoliceNumber();
            attrsBase64[1] = dataUserAttrs.getPoliceType();
            attrsBase64[2] = dataUserAttrs.getPoliceStation();
            //  构造属性集
            Element[] attrs = new Element[attrsBase64.length];
            for (int i = 0; i < attrs.length; i++) {
                try {
                    attrs[i] = HashUtil.h1Zr(attrsBase64[i]);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

            //	零知识证明验证是否注册成功
            Element[] proofCri = dataUserEntityRegistration.generateCriProof(cri, rii, ski, attrs);
            Element[] proofPii = dataUserEntityRegistration.generatePiiProof(pii, ri, ski);
            boolean resultCri = dataUserEntityRegistration.verifyCriProof(proofCri, ri);
            boolean resultPii = dataUserEntityRegistration.verifyPiiProof(proofPii);
            boolean result = resultCri & resultPii;
            if (result) {
                System.out.println("零知识证明证明通过, 数据用户登录成功！！！");
                System.out.println("==================数据用户登录成功==================");
                session.setAttribute("username",username);
                return "welcome";
            } else {
                System.out.println("零知识证明未通过，数据提供者登录失败！！！");
                model.addAttribute("msg", "零知识证明未通过，数据提供者注册失败！！！");
                System.out.println("==================数据用户登录失败==================");
                return "page-login";
            }
        } else {
            model.addAttribute("msg", "无此用户信息");
            System.out.println("==================数据用户登录失败==================");
            return "page-login";
        }
    }
}