package com.becas.exceluploader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class ExcelUploaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcelUploaderApplication.class, args);
    }
}