package com.fosagri.application.security;

import com.fosagri.application.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfiguration(CustomAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Allow access to static resources
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(new AntPathRequestMatcher("/images/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/icons/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/frontend/**")).permitAll()
        );

        // Configure access denied handling
        http.exceptionHandling(ex -> ex
            .accessDeniedPage("/access-denied")
        );

        super.configure(http);

        // Set login view with custom success handler
        setLoginView(http, LoginView.class);

        // Configure form login with custom success handler
        http.formLogin(form -> form
            .successHandler(successHandler)
        );
    }
}
