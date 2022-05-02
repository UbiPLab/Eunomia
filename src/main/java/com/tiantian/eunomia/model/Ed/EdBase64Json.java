package com.tiantian.eunomia.model.Ed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author shubham
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EdBase64Json {

    int ed_id;
    String tel;
    String ed_base64_json;
}
