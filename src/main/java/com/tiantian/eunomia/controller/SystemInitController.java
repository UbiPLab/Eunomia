package com.tiantian.eunomia.controller;

import com.tiantian.eunomia.service.impl.SystemInitServiceImpl;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author tiantian152
 */
@Controller
public class SystemInitController {

    public static Pairing pairing = PairingFactory.getPairing("a1.properties");

    @Autowired
    SystemInitServiceImpl systemInit = new SystemInitServiceImpl();

    @RequestMapping("/systemInit")
    public String systemInit() {
        System.out.println("==================系统初始化开始==================");
        systemInit.systemInit();
        System.out.println("==================系统初始化结束==================");
        return "page-login";
    }
}
