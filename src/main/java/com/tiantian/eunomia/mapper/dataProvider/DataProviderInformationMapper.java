package com.tiantian.eunomia.mapper.dataProvider;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiantian.eunomia.model.dataProvider.DataProviderInformation;
import com.tiantian.eunomia.model.dataReport.Report;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author shubham
 */
@Repository
public interface DataProviderInformationMapper extends BaseMapper<DataProviderInformation> {

    @Select("select * from data_provider_information where tel = #{tel}")
    public List<DataProviderInformation> getBytel(String tel);
}
