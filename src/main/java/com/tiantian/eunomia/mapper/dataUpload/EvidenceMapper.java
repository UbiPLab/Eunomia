package com.tiantian.eunomia.mapper.dataUpload;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiantian.eunomia.model.dataUpload.Evidence;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author shubham
 */
@Repository
public interface EvidenceMapper extends BaseMapper<Evidence> {

    //查询
    @Select("select * from evidence")
    public List<Evidence> getEvidence();

    @Select("select * from evidence where location = #{location} and accident_type = #{accidentType}")
    public List<Evidence> getEvidenceByLAndA(String location,String accidentType);

    @Select("select count(*) from evidence")
    public int count();

}
