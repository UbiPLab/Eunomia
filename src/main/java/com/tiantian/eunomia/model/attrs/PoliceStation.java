package com.tiantian.eunomia.model.attrs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 警察局表
 *
 * @author tiantian152
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PoliceStation {

    /**
     * 派出所序号
     */
    private Integer policeStationId;

    /**
     * 派出所名称
     */
    private String policeStationName;

    /**
     * 分局
     */
    private String policeStationDistrictLevel;

    /**
     * 市局
     */
    private String policeStationCityLevel;

}
