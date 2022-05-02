package com.tiantian.eunomia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiantian.eunomia.model.dataUser.DataUserAttrs;
import com.tiantian.eunomia.model.dataUser.DataUserInformation;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author tiantian152
 */
@Repository
public interface DataUserInformationMapper extends BaseMapper<DataUserInformation> {

    @Select("select * from data_user_information where data_user_id = #{dataUserId}")
    public DataUserInformation getDataUser(int dataUserId);
}
