package com.becas.exceluploader.controller;

import com.becas.exceluploader.service.RechazoMasivoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.becas.exceluploader.entity.AuditoriaRechazoMasivo;
import com.becas.exceluploader.repository.AuditoriaRechazoMasivoRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Controller
public class RechazoMasivoController {

    private final RechazoMasivoService rechazoMasivoService;

    private final AuditoriaRechazoMasivoRepository auditoriaRepo;

    public RechazoMasivoController(
            RechazoMasivoService rechazoMasivoService,
            AuditoriaRechazoMasivoRepository auditoriaRepo
    ) {
        this.rechazoMasivoService = rechazoMasivoService;
        this.auditoriaRepo = auditoriaRepo;
    }

    // 🔹 FORMULARIO RECHAZO MASIVO
    @GetMapping("/rechazo")
    public String formulario() {
        return "RechazoInformacion";
    }

    @PostMapping("/rechazo/procesar")
    public String procesar(
            @RequestParam("archivo") MultipartFile archivo,
            Model model,
            HttpSession session
    ) {

        AuditoriaRechazoMasivo au = new AuditoriaRechazoMasivo();

        String usuario = (String) session.getAttribute("usuarioLogeado");

        try {

            // ========================================
            // PROCESAR EXCEL
            // ========================================

            String mensaje = rechazoMasivoService.procesarExcel(archivo);

            model.addAttribute("resultado", mensaje);

            // ========================================
            // AUDITORIA OK / ERROR
            // ========================================

            au.setUsuario(usuario);
            au.setNombreArchivo(archivo.getOriginalFilename());
            au.setFechaRegistro(LocalDateTime.now());
            au.setMensaje(mensaje);

            if (mensaje.contains("⛔")
                    || mensaje.contains("❌")) {

                au.setEstado("ERROR");

            } else {

                au.setEstado("OK");
            }

            // EXTRAER TOTAL PROCESADOS
            int totalProcesados = 0;

            try {

                if (mensaje.contains("✔️ Procesados:")) {

                    String texto = mensaje.substring(
                            mensaje.indexOf("✔️ Procesados:")
                    );

                    texto = texto.replace("✔️ Procesados:", "").trim();

                    totalProcesados = Integer.parseInt(texto);
                }

            } catch (Exception ex) {
                totalProcesados = 0;
            }

            au.setTotalRegistrosProcesados(totalProcesados);

        } catch (Exception e) {

            model.addAttribute("resultado", e.getMessage());

            au.setUsuario(usuario);
            au.setNombreArchivo(archivo.getOriginalFilename());
            au.setFechaRegistro(LocalDateTime.now());
            au.setEstado("ERROR");
            au.setMensaje(e.getMessage());
            au.setTotalRegistrosProcesados(0);
        }

        auditoriaRepo.save(au);

        return "RechazoInformacion";
    }

}