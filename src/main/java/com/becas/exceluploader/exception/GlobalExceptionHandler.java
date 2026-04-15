package com.becas.exceluploader.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.ResponseEntity;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> manejarArchivoGrande(MaxUploadSizeExceededException ex) {

        return ResponseEntity
                .badRequest()
                .body("❌ El archivo supera los 20MB permitidos.");
    }
}