package com.tiantian.eunomia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 元数据
 *
 * @author tiantian152
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Md {

    /**
     * 案件发生时间
     */
    private Date caseTime;

    /**
     * 案件发生地点
     */
    private String casePlace;

    /**
     * 案件种类
     */
    private String caseType;

    /**
     * 数据类型
     */
    private String dataType;
}
