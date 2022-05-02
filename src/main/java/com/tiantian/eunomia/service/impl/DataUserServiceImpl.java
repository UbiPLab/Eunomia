package com.tiantian.eunomia.service.impl;

import com.tiantian.eunomia.mapper.DataUserMapper;
import com.tiantian.eunomia.model.dataUser.DataUser;
import com.tiantian.eunomia.service.DataUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @author tiantian152
 */
@Service
@Component
public class DataUserServiceImpl implements DataUserService {

    @Autowired
    DataUserMapper dataUserMapper;

    @Override
    public int insert(DataUser dataUser) {
        dataUserMapper.insert(dataUser);
        return dataUser.getDataUserId();
    }
}
