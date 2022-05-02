package com.tiantian.eunomia.model;

import it.unisa.dia.gas.jpbc.Element;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author shubham
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PIj {
    private Element[] proof_cri;
    private Element[] proof_pii;
}
