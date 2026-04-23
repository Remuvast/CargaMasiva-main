package com.becas.exceluploader.controller;

import com.becas.exceluploader.service.ExcelProcessingService;
import com.becas.exceluploader.service.ResultadoCarga;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/excel")
public class ExcelUploadController {

    private final ExcelProcessingService excelService;

    public ExcelUploadController(ExcelProcessingService excelService) {
        this.excelService = excelService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ResultadoCarga> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            ResultadoCarga resultado = excelService.procesarExcel(file);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResultadoCarga("Error: " + e.getMessage(), 0));
        }
    }
}