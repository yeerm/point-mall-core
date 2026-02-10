package com.pointmall.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // @CreatedDate, @LastModifiedDate
@SpringBootApplication
public class PointMallCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(PointMallCoreApplication.class, args);
	}

}
