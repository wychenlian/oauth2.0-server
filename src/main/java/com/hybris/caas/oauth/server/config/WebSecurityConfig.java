//package com.hybris.caas.oauth.server.config;
//
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
//
//    // configure Spring Security, demand authentication and specific scopes
//    public void configure(HttpSecurity http) throws Exception {
//        // @formatter:off
//        http
//                .sessionManagement()
//                // session is created by approuter
//                .sessionCreationPolicy(SessionCreationPolicy.NEVER)
//                .and()
//                // demand specific scopes depending on intended request
//                .authorizeRequests()
//                // enable OAuth2 checks
//                .anyRequest().permitAll(); //used as health check on CF: must be accessible by "anybody"
//        // @formatter:on
//    }
//
//
//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth
//                .inMemoryAuthentication()
//                .withUser("admin").password("123456").roles("USER");
//    }
//
//}