package com.tiantian.eunomia.model.dataProvider;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author shubham
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DataProviderInformation {

    @TableId(value = "data_provider_id", type = IdType.AUTO)
    private int dataProviderId;
    private String name;
    private String email;
    private String tel;
    private String address;
}
