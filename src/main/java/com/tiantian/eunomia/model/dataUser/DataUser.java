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
public class DataUser {

    @TableId(value = "data_user_id", type = IdType.AUTO)
    private Integer dataUserId;
    private String username;
    private String cri;
    private String pii;
    private String ri;
    private String rii;
}
