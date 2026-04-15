package com.becas.exceluploader.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.ui.Model;

@Configuration
@ControllerAdvice
public class AppConfig {

    @Value("${app.logout.url}")
    private String logoutUrl;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("logoutUrl", logoutUrl);
    }
}