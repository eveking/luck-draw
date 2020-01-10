package com.vboly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class LuckDrawApplication {

	public static void main(String[] args) {
		SpringApplication.run(LuckDrawApplication.class, args);
	}

}

