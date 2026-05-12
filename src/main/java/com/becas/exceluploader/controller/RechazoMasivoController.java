package com.becas.exceluploader.controller;

import com.becas.exceluploader.service.RechazoMasivoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class RechazoMasivoController {

    private final RechazoMasivoService rechazoMasivoService;

    public RechazoMasivoController(RechazoMasivoService rechazoMasivoService) {
        this.rechazoMasivoService = rechazoMasivoService;
    }

    // 🔹 FORMULARIO RECHAZO MASIVO
    @GetMapping("/rechazo")
    public String formulario() {
        return "RechazoInformacion";
    }

    @PostMapping("/rechazo/procesar")
    public String procesar(
            @RequestParam("archivo") MultipartFile archivo,
            Model model
    ) {
        try {
            String mensaje = rechazoMasivoService.procesarExcel(archivo);
            model.addAttribute("resultado", mensaje);
        } catch (Exception e) {
            model.addAttribute("resultado", e.getMessage());
        }

        return "RechazoInformacion";
    }
}