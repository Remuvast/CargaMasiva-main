package com.becas.exceluploader.security;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Value("${app.logout.url}")
    private String loginPusak;

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String cleanUri = uri.split(";")[0];

        // Permitir públicos
        if (cleanUri.contains("/css/")
                || cleanUri.contains("/js/")
                || cleanUri.contains("/images/")
                || cleanUri.contains("/webjars/")
                || cleanUri.contains("/sso/login")) {

            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);

        // Sin sesión
        if (session == null
                || session.getAttribute("usuarioLogeado") == null) {

            res.sendRedirect(loginPusak);
            return;
        }

        // BLOQUEAR acceso manual a /menu
        if (cleanUri.endsWith("/menu")) {

            Object acceso = session.getAttribute("accesoSSO");

            // Si viene desde SSO, permitir una sola vez
            if (acceso != null) {
                session.removeAttribute("accesoSSO");
                chain.doFilter(request, response);
                return;
            }

            // Si ya tiene sesión válida, permitir navegación interna
            if (session.getAttribute("usuarioLogeado") != null) {
                chain.doFilter(request, response);
                return;
            }

            res.sendRedirect(loginPusak);
            return;
        }

        // Root siempre manda al login
        if (cleanUri.equals("/")
                || cleanUri.endsWith("/index")
                || cleanUri.endsWith("/")) {

            res.sendRedirect(loginPusak);
            return;
        }

        chain.doFilter(request, response);
    }
}