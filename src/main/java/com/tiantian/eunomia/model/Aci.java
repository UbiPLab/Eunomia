package com.tiantian.eunomia.model;

import it.unisa.dia.gas.jpbc.Element;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author tiantian152
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Aci {

    private Element cri;
    private String[] attr;
    private Element pii;
    private Element auxi;
}
