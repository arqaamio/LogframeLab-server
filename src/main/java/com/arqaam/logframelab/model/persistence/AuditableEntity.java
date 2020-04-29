package com.arqaam.logframelab.model.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.Instant;

@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class AuditableEntity<PK extends Serializable> implements Serializable {

  private transient PK id;

  @CreatedBy
  @Column(name = "CREATED_BY")
  private String createdBy;

  @CreatedDate
  @Column(name = "CREATED_AT", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedBy
  @Column(name = "UPDATED_BY")
  private String updatedBy;

  @LastModifiedDate
  @Column(name = "UPDATED_AT", nullable = false)
  private Instant updatedAt;
}
