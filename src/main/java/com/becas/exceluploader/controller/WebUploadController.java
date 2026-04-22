package com.becas.exceluploader.controller;

import com.becas.exceluploader.service.ExcelProcessingService;

import com.becas.exceluploader.entity.AuditoriaCargaMasiva;
import com.becas.exceluploader.repository.AuditoriaCargaMasivaRepository;
import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebUploadController {

    private final ExcelProcessingService excelService;
    private final AuditoriaCargaMasivaRepository auditoriaRepo;

    public WebUploadController(
            ExcelProcessingService excelService,
            AuditoriaCargaMasivaRepository auditoriaRepo) {

        this.excelService = excelService;
        this.auditoriaRepo = auditoriaRepo;
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
    public String handleFileUpload(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        AuditoriaCargaMasiva au = new AuditoriaCargaMasiva();

        String usuario = (String) session.getAttribute("usuarioLogeado");

        try {
            String resultado = excelService.procesarExcel(file);

            redirectAttributes.addFlashAttribute("resultado", resultado);

            au.setUsuario(usuario);
            au.setNombreArchivo(file.getOriginalFilename());
            au.setFechaRegistro(LocalDateTime.now());
            au.setEstado("OK");
            au.setMensaje(resultado);

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("resultado", "Error: " + e.getMessage());

            au.setUsuario(usuario);
            au.setNombreArchivo(file.getOriginalFilename());
            au.setFechaRegistro(LocalDateTime.now());
            au.setEstado("ERROR");
            au.setMensaje(e.getMessage());
        }

        auditoriaRepo.save(au);

        return "redirect:/carga";
    }
}
