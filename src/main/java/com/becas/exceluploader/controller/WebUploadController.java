package com.becas.exceluploader.controller;

import com.becas.exceluploader.service.ExcelProcessingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebUploadController {

    private final ExcelProcessingService excelService;

    public WebUploadController(ExcelProcessingService excelService) {
        this.excelService = excelService;
    }

// 🔹 PANTALLA PRINCIPAL (2 botones)
    @GetMapping("/menu")
    public String menu() {
        return "IndexCargaInformacion";
    }

    // 🔹 FORMULARIO CARGA MASIVA
    @GetMapping("/carga")
    public String showCarga() {
        return "CargaInformacion";
    }

    // 🔹 FORMULARIO RECHAZO MASIVO
    @GetMapping("/rechazo")
    public String mostrarRechazo() {
        return "RechazoInformacion";
    }

    // 🔹 PROCESO DE CARGA
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            String resultado = excelService.procesarExcel(file);
            redirectAttributes.addFlashAttribute("resultado", resultado);
        } catch (Exception e) {
        redirectAttributes.addFlashAttribute("resultado", "Error: " + e.getMessage());
        }
        return "redirect:/carga";
    }
}
