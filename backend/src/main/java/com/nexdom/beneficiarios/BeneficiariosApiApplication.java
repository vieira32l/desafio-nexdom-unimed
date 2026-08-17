package com.nexdom.beneficiarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BeneficiariosApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeneficiariosApiApplication.class, args);
	}

}
