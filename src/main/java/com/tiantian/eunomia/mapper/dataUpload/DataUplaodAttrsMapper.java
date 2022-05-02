package com.tiantian.eunomia.mapper.dataUpload;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiantian.eunomia.model.dataUpload.DataUploadAttrs;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author shubham
 */
@Repository
public interface DataUplaodAttrsMapper extends BaseMapper<DataUploadAttrs> {

    //查询
    @Select("select * from data_upload_attrs")
    public List<DataUploadAttrs> getDataAttrs();
}
