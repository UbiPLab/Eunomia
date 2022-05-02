package com.tiantian.eunomia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiantian.eunomia.model.dataUser.Cases;
import com.tiantian.eunomia.model.dataUser.DataUserAttrs;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tiantian152
 */
@Repository
public interface DataUserAttrsMapper extends BaseMapper<DataUserAttrs> {

    @Select("select * from data_user_attrs")
    public List<DataUserAttrs> getAttrs();

    @Select("select * from data_user_attrs where data_user_id = #{dataUserId}")
    public DataUserAttrs getDataUser(int dataUserId);
}
