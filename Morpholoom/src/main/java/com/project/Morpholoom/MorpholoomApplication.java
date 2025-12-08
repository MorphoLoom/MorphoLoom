package com.project.Morpholoom;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.project.Morpholoom.mapper")
@SpringBootApplication
public class MorpholoomApplication {

	public static void main(String[] args) {
		SpringApplication.run(MorpholoomApplication.class, args);
	}

}
