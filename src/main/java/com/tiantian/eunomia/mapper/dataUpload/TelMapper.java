package com.tiantian.eunomia.mapper.dataUpload;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiantian.eunomia.model.dataUpload.Evidence;
import com.tiantian.eunomia.model.dataUpload.Tel;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author shubham
 */
@Repository
public interface TelMapper extends BaseMapper<Tel> {

    //查询
    @Select("select * from tel")
    public List<Tel> getTel();

    @Select("select count(*) from tel1 where tel = #{tel} GROUP BY tel")
    public int getNumber(String tel);


}
