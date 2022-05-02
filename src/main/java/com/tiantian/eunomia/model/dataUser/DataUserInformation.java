package com.tiantian.eunomia.model.dataUser;

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
public class DataUserInformation {

    @TableId(value = "data_user_information_id", type = IdType.AUTO)
    private Integer dataUserInformationId;
    private Integer dataUserId;
    private String name;
    private String email;
    private String tel;
    private String address;
}
