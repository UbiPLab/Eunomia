package com.tiantian.eunomia.model.attrs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 街道表
 * 表名街道与警察局的对应关系
 *
 * @author tiantian152
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Street {

    /**
     * 街道序号
     */
    private Integer streetId;

    /**
     * 街道名
     */
    private String streetName;

    /**
     * 区
     */
    private String district;

    /**
     * 市
     */
    private String city;

    /**
     * 省
     */
    private String province;

    /**
     * 派出所序号
     */
    private Integer policeStationId;
}
