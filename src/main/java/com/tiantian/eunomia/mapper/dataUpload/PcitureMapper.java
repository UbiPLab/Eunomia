package com.tiantian.eunomia.mapper.dataUpload;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiantian.eunomia.model.dataUpload.Picture;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author shubham
 */
@Repository
public interface PcitureMapper extends BaseMapper<Picture> {

    //查询
    @Select("select * from picture")
    public List<Picture> selectPicture();
}
