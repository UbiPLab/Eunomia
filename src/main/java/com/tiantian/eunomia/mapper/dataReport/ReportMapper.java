package com.tiantian.eunomia.mapper.dataReport;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiantian.eunomia.model.dataReport.Report;
import com.tiantian.eunomia.model.dataUpload.Evidence;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author shubham
 */
@Repository
public interface ReportMapper extends BaseMapper<Report> {

    //查询
    @Select("select * from report")
    public List<Report> getReport();

    @Select("select * from report where username = #{username}")
    public List<Report> getReportByUsername(String username);

    @Select("select * from report where case_id = #{caseId}")
    public List<Report> getReportByCase(String username);

    @Select("select * from report where evidence_id = #{evidenceId}")
    public List<Report> getReportByEvidence(String username);

    @Select("select count(*) from report")
    public int count();
}
