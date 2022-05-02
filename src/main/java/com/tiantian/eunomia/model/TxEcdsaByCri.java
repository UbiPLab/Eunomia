package com.tiantian.eunomia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author tiantian152
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TxEcdsaByCri {

    private String criBase64;
    private String ts = String.valueOf(System.currentTimeMillis());
    private String txEcdsaBase64;
}
