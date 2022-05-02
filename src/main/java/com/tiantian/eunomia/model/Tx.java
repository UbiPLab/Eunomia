package com.tiantian.eunomia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author tiantian152
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tx {

    private String txJson;
    private String txEcdsaJson;
}
