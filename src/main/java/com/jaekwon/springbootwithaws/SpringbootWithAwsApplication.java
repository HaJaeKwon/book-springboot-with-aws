package com.jaekwon.springbootwithaws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// @WebMvcTest 에서 스캔하게 되어 JPA metamodel must not be empty! 에러가 발생한다
// ComponenScan 대상에서 제외 될 수 있도록 @EnableJpaAuditing 을 분리해야 한다
//@EnableJpaAuditing
@SpringBootApplication
public class SpringbootWithAwsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootWithAwsApplication.class, args);
	}

}
