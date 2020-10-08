package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.model.persistence.SDGCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Set;

public interface SDGCodeRepository extends JpaRepository<SDGCode, Long>{
    public Set<SDGCode> findByIdIn(Collection<Long> ids);
}
