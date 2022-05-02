package com.tiantian.eunomia.model.Ed;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author tiantian152
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EdBase64 {

    
    String[] ct0;
    Map<String, String[]> ctiv;
    String ctti;
    String di;
}
