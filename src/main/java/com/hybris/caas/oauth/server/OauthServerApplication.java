package com.hybris.caas.oauth.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = {"com.hybris.caas.log", "com.hybris.caas.multitenancy", "com.hybris.caas.oauth.server"})
public class OauthServerApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(OauthServerApplication.class, args);
	}
}
