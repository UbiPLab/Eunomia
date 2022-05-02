package com.tiantian.eunomia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiantian.eunomia.model.SystemInit;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tiantian152
 */
@Repository
public interface SystemInitViewMapper extends BaseMapper<SystemInit> {

    /**
     * 查询所有systemInits
     *
     * @return systemInits
     */
    @Select("select * from system_init_view")
    List<SystemInit> selectAll();
}
