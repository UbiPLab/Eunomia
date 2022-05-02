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
public class H {

    @TableId(value = "h_id", type = IdType.AUTO)
    private Integer hId;
    private String h1, h2, h3, h4, h5;
}
