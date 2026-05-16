package com.lifetrack;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lifetrack.mapper")
public class LifetrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifetrackApplication.class, args);
    }
}
