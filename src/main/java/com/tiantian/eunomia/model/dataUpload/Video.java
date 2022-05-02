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
public class Video {

    @TableId(value = "video_id", type = IdType.AUTO)
    private int videoId;
    private String url;
    private String name;
    private String path;
    private String hash;
}
