package com.becas.exceluploader.repository;

import com.becas.exceluploader.entity.AuditoriaCargaMasiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaCargaMasivaRepository
        extends JpaRepository<AuditoriaCargaMasiva, Long> {
}