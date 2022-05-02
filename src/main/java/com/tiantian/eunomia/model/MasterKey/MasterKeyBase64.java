package com.tiantian.eunomia.model.MasterKey;

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
public class MasterKeyBase64 {

    private String[] sk1;
    private Map<String, String[]> skx;
    private String[] sk3;
}
