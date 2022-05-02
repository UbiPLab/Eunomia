package com.tiantian.eunomia.model.view;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author tiantian152
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemInitView {

    private Integer systemInitId;

    /**
     * 两个素数的乘积
     */
    private String N;

    /**
     * 数组（如果属性集atti是三个属性，长度就是5）
     */
    private Integer hId;

    private String h1, h2, h3, h4, h5;
}
