package com.tiantian.eunomia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiantian.eunomia.model.dataUser.Cases;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author shubham
 */
@Repository
public interface CasesMapper extends BaseMapper<Cases> {
    //查询
    @Select("select * from cases")
    public List<Cases> getCase();

    @Select("select * from cases where location = #{location} and accident_type = #{accidentType}")
    public List<Cases> getCaseByLAndA(String location, String accidentType);

    @Select("select count(*) from cases")
    public int count();

    @Update("update cases set stage = #{stage} where case_id = #{caseId}")
    void updateStage(String stage,int caseId);





}
