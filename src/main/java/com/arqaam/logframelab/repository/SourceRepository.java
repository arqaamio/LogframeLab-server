package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.model.persistence.Source;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Set;

public interface SourceRepository extends JpaRepository<Source, Long> {

    public Set<Source> findByIdIn(Collection<Long> ids);
}
