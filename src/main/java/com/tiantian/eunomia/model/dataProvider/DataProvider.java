package com.tiantian.eunomia.model.dataProvider;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author shubham
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DataProvider {

    @TableId(value = "data_provider_id", type = IdType.AUTO)
    private int dataProviderId;
    private String cri;
    private String pii;
    private String ri;
    private String rii;
}
