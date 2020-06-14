package com.arqaam.logframelab.model.persistence.auth;

import com.arqaam.logframelab.model.persistence.AuditableEntity;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "GroupAuthority")
@Table(name = "GROUP_AUTHORITIES")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class GroupAuthority extends AuditableEntity<GroupAuthorityId> {

  @EmbeddedId
  private GroupAuthorityId id;
}

@Getter
@ToString
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
class GroupAuthorityId implements Serializable {

  @Column(name = "GROUP_ID")
  private Integer groupId;

  @Column(name = "AUTHORITY")
  private String authority;
}
