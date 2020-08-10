package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.model.persistence.Source;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceRepository extends JpaRepository<Source, Long> {
}
