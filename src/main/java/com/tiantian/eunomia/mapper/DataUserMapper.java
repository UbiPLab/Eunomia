package com.tiantian.eunomia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiantian.eunomia.model.dataUser.DataUser;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tiantian152
 */
@Repository
public interface DataUserMapper extends BaseMapper<DataUser> {

    /**
     * 按照用户名查询数据用户
     *
     * @param username 用户名
     * @return DataUserList
     */
    @Select("select * from data_user where username = #{username}")
    List<DataUser> selectDataUserByUsername(String username);

    @Select("select count(*) from data_user")
    public int count();


}
