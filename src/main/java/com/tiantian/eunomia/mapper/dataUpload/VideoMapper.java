package com.tiantian.eunomia.mapper.dataUpload;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiantian.eunomia.model.dataUpload.Video;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author shubham
 */
@Repository
public interface VideoMapper extends BaseMapper<Video> {

    //查询
    @Select("select * from video")
    public List<Video> selectVideo();
}
