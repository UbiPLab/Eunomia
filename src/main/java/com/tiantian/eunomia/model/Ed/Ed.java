package com.tiantian.eunomia.model.Ed;

import it.unisa.dia.gas.jpbc.Element;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ed {
    public Element[] ct0;
    public Map<String, Element[]> ctiv;
    public Element ctti;
    public Element di;

    public Element[] getCt0() {
        return ct0;
    }

    public Map<String, Element[]> getCtiv() {
        return ctiv;
    }

    public Element getCtti() {
        return ctti;
    }

    public Element getDi() {
        return di;
    }
}
