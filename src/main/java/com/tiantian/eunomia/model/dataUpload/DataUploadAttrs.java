package com.tiantian.eunomia.model.dataUpload;

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
public class DataUploadAttrs {

    @TableId(value = "file_id", type = IdType.AUTO)
    private int fileId;
    private String time;
    private String location;
    private String accidentType;
}
