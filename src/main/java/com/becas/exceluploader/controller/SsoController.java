package com.becas.exceluploader.controller;

import java.security.MessageDigest;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/sso")
public class SsoController {

    @Value("${app.logout.url}")
    private String loginPusak;

    @Value("${app.sso.secret}")
    private String secreto;

    @GetMapping("/login")
    public String loginSSO(
            @RequestParam String u,
            @RequestParam String r,
            @RequestParam long ts,
            @RequestParam String t,
            HttpSession session) {

        try {

            long ahora = System.currentTimeMillis();

            // Expira en 2 minutos
            if ((ahora - ts) > 120000) {
                return "redirect:" + loginPusak;
            }

            String data = u + "|" + r + "|" + ts + "|" + secreto;

            String tokenLocal = sha256(data);

            if (!tokenLocal.equals(t)) {
                return "redirect:" + loginPusak;
            }

            // Crear sesión segura
            session.setAttribute("usuarioLogeado", u);
            session.setAttribute("rol", r);
            session.setAttribute("accesoSSO", true);

            // Entrar al menú
            return "forward:/menu";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:" + loginPusak;
        }
    }

    private String sha256(String texto) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(texto.getBytes("UTF-8"));

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}