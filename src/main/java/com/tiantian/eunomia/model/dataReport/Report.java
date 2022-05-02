package com.tiantian.eunomia.model.dataReport;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author shubham
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Report {

    @TableId(value = "report_id", type = IdType.AUTO)
    private int reportId;
    private int caseId;
    private int evidenceId;
    private String username;
    private String report;
}
