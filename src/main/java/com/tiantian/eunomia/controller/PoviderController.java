package com.tiantian.eunomia.controller;

import com.alibaba.fastjson.JSONObject;
import com.tiantian.eunomia.mapper.dataProvider.DataProviderInformationMapper;
import com.tiantian.eunomia.mapper.dataProvider.DataProviderMapper;
import com.tiantian.eunomia.model.Aci;
import com.tiantian.eunomia.model.MasterKey.MasterKey;
import com.tiantian.eunomia.model.PkMsk.PkMsk;
import com.tiantian.eunomia.model.Tx;
import com.tiantian.eunomia.model.TxEcdsa;
import com.tiantian.eunomia.model.dataProvider.DataProviderInformation;
import com.tiantian.eunomia.model.dataProvider.DataProvider;
import com.tiantian.eunomia.service.*;
import com.tiantian.eunomia.service.impl.*;
import com.tiantian.eunomia.utils.Base64Util;
import com.tiantian.eunomia.utils.HashUtil;
import com.tiantian.eunomia.utils.HttpCallOtherInterfaceUtils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shubham
 */
@Controller
public class PoviderController {

    @Autowired
    private DataProviderMapper dataProviderMapper;

    @Autowired
    private DataProviderInformationMapper dataProviderInformationMapper;

    @Autowired
    private DataUserEntityRegistration dataUserEntityRegistration = new DataUserEntityRegistrationImpl();

    @Autowired
    private DataProviderEntityRegistration dataProviderEntityRegistration = new DataProviderEntityRegistrationImpl();

    @Autowired
    private SystemInitService systemInit = new SystemInitServiceImpl();

    //	生成pairing对象;
    private static final Pairing PAIRING = PairingFactory.getPairing("a1.properties");

    //数据提供者登录
    @RequestMapping("/providerLogin")
    public String providerLogin(@RequestParam("tel") String tel,
                                @RequestParam("password") String password,
                                Model model,
                                HttpSession session){
        System.out.println(tel);
        System.out.println(password);
        System.out.println("==================数据用户登录开始==================");

        //	生成主密钥ski
        Element ski = dataUserEntityRegistration.generateSki(password);

        Map<String, Object> map = new HashMap<>(1);
        map.put("tel", tel);
        System.out.println("map="+map);
        List<DataProviderInformation> login = dataProviderInformationMapper.getBytel(tel);
        System.out.println("login"+login.size());


        if(login.size()!=0){

            boolean saveSkiResult = dataProviderEntityRegistration.saveSki(ski,tel);
            //获取所有Cri以及自己的Cri
            List<String> criBase64 = dataProviderMapper.getAllCri();
            System.out.println(criBase64.get(0).toString());
            String telCriBase64 = dataProviderEntityRegistration.readCri(tel);
            String telCriBase64String = telCriBase64.replace("\"","").replace("\"","");
            System.out.println(telCriBase64String);
            Element telCri = Base64Util.base64ToElement(telCriBase64String);
            Element telCriForZr = Base64Util.base64ToElementForZr(telCriBase64String);

            Map<String, Object> map2 = new HashMap<>(1);
            map2.put("cri", telCriBase64String);

            //查找tel所对应的属性以及telCri所对应的pii,ri,rii
            String riiBase64 = dataProviderMapper.selectByMap(map2).get(0).getRii();
            String riBase64 = dataProviderMapper.selectByMap(map2).get(0).getRi();
            String piiBase64 = dataProviderMapper.selectByMap(map2).get(0).getPii();
            Element rii = Base64Util.base64ToElementForZr(riiBase64);
            Element ri = Base64Util.base64ToElementForZr(riBase64);
            Element pii = Base64Util.base64ToElement(piiBase64);


            Map<String, Object> map3 = new HashMap<>(1);
            map3.put("tel", tel);
            String[] attrsBase64 = new String[3];
            attrsBase64[0] = dataProviderInformationMapper.selectByMap(map3).get(0).getName();
            attrsBase64[1] = dataProviderInformationMapper.selectByMap(map3).get(0).getEmail();
            attrsBase64[2] = dataProviderInformationMapper.selectByMap(map3).get(0).getTel();
            //  构造属性集
            Element[] attrs = new Element[attrsBase64.length];
            for (int i=0;i<attrs.length;i++){
                try {
                    attrs[i] = HashUtil.h1Zr(attrsBase64[i]);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }


            //获取ci
            Element ci[] = new Element[criBase64.size()];
            for(int i=0;i<criBase64.size();i++){
                ci[i] = Base64Util.base64ToElementForZr(criBase64.get(i).toString());
            }

            BigInteger N = systemInit.generatePrime();
            //生成Ai和wi
            BigInteger Ai = dataProviderEntityRegistration.calculateAi(ci,N);
            BigInteger wi = dataProviderEntityRegistration.calculateWi(ci,telCriForZr,N);
            Element proofWi = PAIRING.getZr().newElement(wi);
            //零知识证明验证
            boolean resultA = dataProviderEntityRegistration.verifyAi(Ai,wi,telCriForZr,N);
            System.out.println(resultA);
            Element[] proofCri =  dataUserEntityRegistration.generateCriProof(telCri,rii,ski,attrs);
            boolean resultB = dataUserEntityRegistration.verifyCriProof(proofCri,ri);
            System.out.println(resultB);
            Element[] proofPii =  dataUserEntityRegistration.generatePiiProof(pii,ri,ski);
            boolean resultC = dataUserEntityRegistration.verifyPiiProof(proofPii);
            System.out.println(resultC);
            boolean result = resultA & resultB & resultC;
            System.out.println(result);
            if(result){
                session.setAttribute("login",tel);
                System.out.println("零知识证明证明通过, 数据提供者登录成功！！！");
                System.out.println("==================数据提供者登录成功==================");
                return "index";
//                return true;
            }else {
                System.out.println("零知识证明未通过，数据提供者登录失败！！！");
                model.addAttribute("msg","零知识证明未通过，数据提供者注册失败！！！");
                System.out.println("==================数据提供者登录失败==================");
                return "Provider/provider-login";
//                return false;
            }


        }else {
            model.addAttribute("msg","无此用户信息");
            System.out.println("==================数据用户登录失败==================");
            return "Provider/provider-login";
//            return false;
        }
    }

    //数据提供者注册
    @RequestMapping("/providerRegister")
    public String providerRegister(@RequestParam("password")String password,
                                   @RequestParam("name")String name,
                                   @RequestParam("email")String email,
                                   @RequestParam("tel")String tel,
                                   @RequestParam("address")String address,
                                   HttpServletRequest request,
                                   Model model,
                                   RedirectAttributes redirectAttributes){
        System.out.println("==================实体注册开始==================");

        //	生成主密钥ski
        Element ski = dataUserEntityRegistration.generateSki(password);

        //  随机选择ri属于Zq
        Element ri = dataUserEntityRegistration.generateRi();

        //  计算伪身份pii
        Element pii = dataUserEntityRegistration.calculatePii(ri,ski);

        Element rii = PAIRING.getZr().newRandomElement().getImmutable();

        //  计算凭证cri
        Element cri = dataUserEntityRegistration.calculateCri(rii,ski);

        String criBase64 = Base64Util.elementToBase64(cri);
        String piiBase64 = Base64Util.elementToBase64(pii);
        String riBase64 = Base64Util.elementToBase64(ri);
        String riiBase64 = Base64Util.elementToBase64(rii);

        //保存cri,pii
        DataProvider providerAttrs = new DataProvider(0,criBase64,piiBase64,riBase64,riiBase64);

        DataProviderInformation registerProvider = new DataProviderInformation(0, name, email, tel, address);
        // 检查电话号码是否存在
        Map<String, Object> map = new HashMap<>(1);
        map.put("tel", registerProvider.getTel());
        List<DataProviderInformation> dataProviderInformations = dataProviderInformationMapper.selectByMap(map);
        if(dataProviderInformations.isEmpty()){
            // 该电话号码未被使用，则继续



            // dp_i 向 cb 提交实体注册交易(Tx1)
            // 生成没有签名的Tx1
            Element auxi = ski;
            String[] attr = new String[3];
            attr[0] = name;
            attr[1] = email;
            attr[2] = tel;
            Aci aci = new Aci(cri,attr,pii,auxi);
            JSONObject tx1Json = dataProviderEntityRegistration.generateTX1Json(tel, aci);

            // 生成签名后的Tx1,以及公钥
            TxEcdsa tx1Ecdsa = dataProviderEntityRegistration.generateTx1(tel, aci);
            JSONObject tx1EcdsaJson = (JSONObject) JSONObject.toJSON(tx1Ecdsa);
            System.out.println("tx1EcdsaJson="+tx1EcdsaJson);

            // 生成总数据包
            Tx tx = new Tx(tx1Json.toJSONString(), tx1EcdsaJson.toJSONString());
            JSONObject txJson = (JSONObject) JSONObject.toJSON(tx);
            System.out.println("Tx1:" + txJson);

            //存储用户名和Cri
            dataProviderEntityRegistration.saveCri(criBase64,tel);
            System.out.println("用户名和Cri保存成功！");

            // 发送给区块链
            String sr = HttpCallOtherInterfaceUtils.doPost("verifyTx1", txJson, request);
            System.out.println("返回参数：" + sr);
//            String sr = "true";

            if (Boolean.parseBoolean(sr)) {

                // 保存用户信息
                int dataProviderId = dataProviderMapper.insert(providerAttrs);
                DataProviderInformation dataProviderInformation = new DataProviderInformation(dataProviderId,name,email,tel,address);
                int dataProviderInformationResult = dataProviderInformationMapper.insert(dataProviderInformation);
                System.out.println("dataProviderInformation插入结果= "+ dataProviderInformationResult);
                // 回显用户名
                model.addAttribute("tel", tel);

                // 将属性秘钥保存在本地
                PkMsk pkMsk = dataUserEntityRegistration.generatePkMsk();
                boolean savePkMskResult = dataUserEntityRegistration.savePkMsk(pkMsk, tel);
                MasterKey sKey = dataUserEntityRegistration.generateSKey(attr, pkMsk.getMsk());
                boolean saveSKeyResult = dataUserEntityRegistration.saveSKey(sKey, tel);
                if (savePkMskResult && saveSKeyResult) {
                    System.out.println("属性密钥保存成功");
                }

                System.out.println("==================实体注册成功==================");
                String url = "http://localhost:8091/toWeb3AddTx?showType=cri&functionName=addDataProvider&criOrIndex="+Base64Util.elementToBase64(cri)+"&json="+tx1Ecdsa.getTxEcdsaBase64();

                return "redirect:" + url;
//                return "Provider/provider-login";
            } else {
                System.out.println("==================实体注册失败==================");
                // 写回除用户名密码之外的全部数据
                model.addAttribute("name", name);
                model.addAttribute("email", email);
                model.addAttribute("tel", tel);
                model.addAttribute("address", address);
                return "Provider/provider-register";
            }
        }else {
            model.addAttribute("msg","该用户名已经被使用");
            // 写回除用户名密码之外的全部数据
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("tel", tel);
            model.addAttribute("address", address);
            System.out.println("==================实体注册失败==================");
            return "Provider/provider-register";
        }
    }
}
