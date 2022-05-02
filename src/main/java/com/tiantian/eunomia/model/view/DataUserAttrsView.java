package com.tiantian.eunomia.model.view;

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
public class DataUserAttrsView {

    @TableId(value = "data_user_id", type = IdType.AUTO)
    private Integer dataUserId;
    private String username;
    private String policeNumber;
    private String policeType;
    private String policeStation;
}
