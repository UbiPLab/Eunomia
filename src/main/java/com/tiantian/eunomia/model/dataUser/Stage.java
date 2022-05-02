package com.tiantian.eunomia.model.dataUser;

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
public class Stage {
    private String caseId;
    private String stage;
}
