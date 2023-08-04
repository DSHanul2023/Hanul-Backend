package com.example.hanul.config;

import com.example.hanul.security.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use BCryptPasswordEncoder for password encoding
    }

//    @Override
//    protected void configure(HttpSecurity http) {
//        try {
//            http.cors()
//                    .and()
//                    .csrf().disable()
//                    .httpBasic().disable()
//                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                    .and()
//                    .authorizeRequests().antMatchers("/", "/members/**", "/items/**").permitAll()
//                    .anyRequest().authenticated();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        http.addFilterAfter(
//                jwtAuthenticationFilter,
//                CorsFilter.class
//        );
//    }

    @Override
    protected void configure(HttpSecurity http) {
        try {
            http.cors()
                    .and()
                    .csrf().disable()
                    .httpBasic().disable()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .authorizeRequests().antMatchers("/", "/members/**", "/items/**").permitAll()
                    .antMatchers("/getMemberInfo").authenticated() // Add this line to secure /getMemberInfo
                    .anyRequest().authenticated();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        http.addFilterAfter(
                jwtAuthenticationFilter,
                CorsFilter.class
        );
    }
}
