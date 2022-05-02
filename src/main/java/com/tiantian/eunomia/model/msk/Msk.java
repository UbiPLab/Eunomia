package com.tiantian.eunomia.model.msk;

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
public class Msk {
    private Element g1, g2, a1, a2, c1, c2, d1, d2, d3;

    public Element getG1() {
        return g1.getImmutable();
    }

    public Element getG2() {
        return g2.getImmutable();
    }

    public Element getA1() {
        return a1.getImmutable();
    }

    public Element getA2() {
        return a2.getImmutable();
    }

    public Element getC1() {
        return c1.getImmutable();
    }

    public Element getC2() {
        return c2.getImmutable();
    }

    public Element getD1() {
        return d1.getImmutable();
    }

    public Element getD2() {
        return d2.getImmutable();
    }

    public Element getD3() {
        return d3.getImmutable();
    }
}


