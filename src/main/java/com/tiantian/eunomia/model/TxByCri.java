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
public class TxByCri {

    private String txJson;
    private String txEcdsaByCriJson;
}
