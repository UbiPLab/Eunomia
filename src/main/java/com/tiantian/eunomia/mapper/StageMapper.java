package com.tiantian.eunomia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiantian.eunomia.model.dataUser.Cases;
import com.tiantian.eunomia.model.dataUser.Stage;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author shubham
 */
@Repository
public interface StageMapper extends BaseMapper<Stage> {

    @Select("select * from stage")
    public List<Stage> getStage();

    @Select("select * from stage where case_id = #{caseId}")
    public List<Stage> getStageById(String caseId);
}
