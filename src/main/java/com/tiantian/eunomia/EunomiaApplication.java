package com.tiantian.eunomia;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com/tiantian/eunomia/mapper")
public class EunomiaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EunomiaApplication.class, args);
    }
}
