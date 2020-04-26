package com.arqaam.indicator.configuration.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity(debug = true)
@EnableJpaRepositories
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

	public AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}

	protected void configure(AuthenticationManagerBuilder authBuilder) throws Exception {
		authBuilder.jdbcAuthentication().groupAuthoritiesByUsername("");
	}

}
