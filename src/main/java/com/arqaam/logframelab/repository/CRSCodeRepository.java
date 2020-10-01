package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.model.persistence.CRSCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Set;

public interface CRSCodeRepository extends JpaRepository<CRSCode, Long> {

    public Set<CRSCode> findByIdIn(Collection<Long> ids);
}
