package com.tiantian.eunomia.model.dataUser;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shubham
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Cases {

    @TableId(value = "case_id", type = IdType.AUTO)
    private int caseId;
    private String username;
    private String time;
    private String location;
    private String accidentType;
    private String dataType;
    private String caseName;
    private String stage;

}
