package com.tiantian.eunomia.model.dataUser;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author tiantian152
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataUserAttrs {

    @TableId(value = "data_user_attrs_id", type = IdType.AUTO)
    private Integer dataUserAttrsId;
    private Integer dataUserId;
    private String policeNumber;
    private String policeType;
    private String policeStation;
    private Date policeStartTime;
    private Date policeEndTime;
}
