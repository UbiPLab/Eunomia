package com.tiantian.eunomia.model.MasterKey;

import it.unisa.dia.gas.jpbc.Element;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author tiantian152
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MasterKey {

    private Element[] sk1;
    private Map<String, Element[]> skx;
    private Element[] sk3;
}
