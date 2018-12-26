package com.imooc;

import com.imooc.utils.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages="com.imooc.mapper")
@ComponentScan(basePackages= {"com.imooc", "org.n3r.idworker"})
public class Application {

	
	@Bean
	public SpringUtil getSpingUtil() {
		return new SpringUtil();
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
