package com.fosagri.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@EntityScan("com.fosagri.application.model")
@EnableJpaRepositories("com.fosagri.application.repository")
@PageTitle("FOS AGRI Data")
@Theme(value = "data-app")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
