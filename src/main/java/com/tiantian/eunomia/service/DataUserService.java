package com.tiantian.eunomia.service;

import com.tiantian.eunomia.model.dataUser.DataUser;

/**
 * @author tiantian152
 */
public interface DataUserService {

    /**
     * 插入dataUser，并返回dataUserId
     *
     * @param dataUser dataUser
     * @return dataUserId
     */
    int insert(DataUser dataUser);
}
