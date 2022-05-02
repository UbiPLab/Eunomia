package com.tiantian.eunomia.mapper.dataUpload;

import com.tiantian.eunomia.model.Ed.EdBase64Json;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Component
public interface EdMapper {


    @Select("select * from ed_base64_json")
    List<EdBase64Json> getEdBase64Json();

    @Select("select * from ed_base64_json where tel = #{tel}")
    EdBase64Json selectEdByTel(String tel);

    @Insert("insert into ed_base64_json(ed_id,tel,ed_base64_json) values(#{ed_id},#{tel},#{ed_base64_json})")
    void insertEd(EdBase64Json edBase64Json);


}
