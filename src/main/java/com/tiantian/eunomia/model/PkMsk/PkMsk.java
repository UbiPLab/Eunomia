package com.tiantian.eunomia.model.PkMsk;

import com.tiantian.eunomia.model.msk.Msk;
import com.tiantian.eunomia.model.pk.Pk;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author tiantian152
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PkMsk {

    private Msk msk;
    private Pk pk;
}
