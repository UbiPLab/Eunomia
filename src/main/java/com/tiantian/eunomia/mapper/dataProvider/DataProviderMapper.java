package com.tiantian.eunomia.mapper.dataProvider;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiantian.eunomia.model.dataProvider.DataProvider;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author shubham
 */
@Repository
public interface DataProviderMapper extends BaseMapper<DataProvider> {

    @Select("select * from data_provider")
    public List<DataProvider> getAllProvider();

    @Select("select cri from data_provider")
    public List<String> getAllCri();

    @Select("select count(*) from data_provider")
    public int count();
}
