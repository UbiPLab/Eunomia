package com.tiantian.eunomia.model.PkMsk;

import com.tiantian.eunomia.model.msk.MskBase64;
import com.tiantian.eunomia.model.pk.PkBase64;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author tiantian152
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PkMskBase64 {

    private MskBase64 mskBase64;
    private PkBase64 pkBase64;
}
