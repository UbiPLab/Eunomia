package com.tiantian.eunomia.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author tiantian152
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemInit {

    @TableId(value = "system_init_id", type = IdType.AUTO)
    private Integer systemInitId;
    /**
     * 两个素数的乘积
     */
    private String N;
    /**
     * 数组（如果属性集atti是三个属性，长度就是5）
     */
    private Integer hId;
}
