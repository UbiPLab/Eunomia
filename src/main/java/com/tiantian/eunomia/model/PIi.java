package com.tiantian.eunomia.model;

import it.unisa.dia.gas.jpbc.Element;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PIi {

    private Element[] proof_cri;
    private Element[] proof_pii;
    private Element proof_wi;
}
